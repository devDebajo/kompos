package ru.debajo.kompos

import android.graphics.Canvas
import ru.debajo.kompos.komposifier.DefaultKomposNodeVisualizer
import ru.debajo.kompos.komposifier.KomposMeasurePolicyVisualizer
import ru.debajo.kompos.komposifier.KomposNodeVisualizer
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.komposifier.then

class KomposNode(
    private val density: KomposDensity,
    private val name: String,
    private val renderAction: KomposRenderAction,
    private val measurePolicy: KomposSingleMeasurePolicy,
    private val touchHandler: KomposTouchHandler,
) {
    private val renderScope: MutableKomposRenderScope = MutableKomposRenderScope {
        with(renderAction) {
            render()
        }
        for (nestedNode in nestedNodes) {
            nestedNode.draw(canvas, size)
        }
    }
    private val measureScope: KomposMeasureScopeImpl = KomposMeasureScopeImpl(density)
    private val nestedNodes: MutableList<KomposNode> = mutableListOf()
    var childMeasurePolicy: KomposMeasurePolicy = DefaultKomposMeasurePolicy

    fun draw(canvas: Canvas, size: KomposSize) {
        renderScope.size = size
        renderScope.configure(canvas, density)
        renderScope.drawContent()
    }

    fun addChild(node: KomposNode) {
        if (this === node) {
            error("could not add node in self")
        }
        nestedNodes.add(node)
    }

    fun onTouch(touchEvent: KomposTouchEvent): Boolean {
        return if (nestedNodes.any { it.onTouch(touchEvent) }) {
            true
        } else {
            touchHandler.onTouch(touchEvent)
        }
    }

    fun asMeasurable(): KomposMeasurable {
        return object : KomposMeasurable {
            override fun measure(constraints: KomposConstraints): KomposPlaceable {
                val measureResult = with(measurePolicy) {
                    measureScope.measure(asMeasurableInternal(), constraints)
                }

                return object : KomposPlaceable {
                    override val size: KomposSize
                        get() = KomposSize.create(measureResult.width, measureResult.height)

                    override fun placeAt(x: Int, y: Int) {
                        measureResult.placeChildren(x, y)
                    }
                }
            }
        }
    }

    override fun toString(): String = "KomposNode($name)"

    private fun asMeasurableInternal(): KomposMeasurable {
        return object : KomposMeasurable {
            override fun measure(constraints: KomposConstraints): KomposPlaceable {
                val measureResult = this@KomposNode.measure(constraints)
                return asPlaceable(measureResult)
            }
        }
    }

    private fun measure(constraints: KomposConstraints): KomposMeasureResult {
        return with(childMeasurePolicy) {
            measureScope.measure(
                nestedNodes.map { it.asMeasurable() },
                constraints,
            )
        }
    }

    private fun asPlaceable(measureResult: KomposMeasureResult): KomposPlaceable {
        return object : KomposPlaceable {
            override val size: KomposSize
                get() = KomposSize.create(measureResult.width, measureResult.height)

            override fun placeAt(x: Int, y: Int) {
                measureResult.placeChildren(x, y)
            }
        }
    }
}

fun KomposScope.createNode(name: String, komposifier: Komposifier): KomposNode {
    val visualizer = komposifier.createVisualizer(DefaultKomposNodeVisualizer)
    return KomposNode(
        density = this,
        name = name,
        renderAction = visualizer,
        measurePolicy = visualizer,
        touchHandler = visualizer,
    )
}

interface KomposMeasurable {
    fun measure(constraints: KomposConstraints): KomposPlaceable
}

interface KomposPlaceable {
    val size: KomposSize

    fun placeAt(x: Int, y: Int)

    interface PlaceableScope {
        fun KomposPlaceable.place(x: Int = 0, y: Int = 0)
    }
}

fun interface KomposMeasurePolicy {
    fun KomposMeasureScope.measure(
        measurables: List<KomposMeasurable>,
        constraints: KomposConstraints,
    ): KomposMeasureResult
}

object DefaultKomposMeasurePolicy : KomposMeasurePolicy {
    override fun KomposMeasureScope.measure(
        measurables: List<KomposMeasurable>,
        constraints: KomposConstraints
    ): KomposMeasureResult {
        val placeables = measurables.map { it.measure(constraints) }
        val width = placeables.maxOfOrNull { it.size.width }
        val height = placeables.maxOfOrNull { it.size.height }
        return layout(width ?: 0, height ?: 0) {
            for (placeable in placeables) {
                placeable.place()
            }
        }
    }
}

fun interface KomposSingleMeasurePolicy {
    fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints,
    ): KomposMeasureResult
}

fun KomposSingleMeasurePolicy.asMeasurable(
    measurable: KomposMeasurable,
    measureScope: KomposMeasureScope
): KomposMeasurable {
    val receiver = this
    return object : KomposMeasurable {
        override fun measure(constraints: KomposConstraints): KomposPlaceable {
            val result = with(receiver) {
                measureScope.measure(measurable, constraints)
            }

            return object : KomposPlaceable {
                override val size: KomposSize
                    get() = KomposSize.create(result.width, result.height)

                override fun placeAt(x: Int, y: Int) {
                    result.placeChildren(x, y)
                }
            }
        }
    }
}

object DefaultKomposSingleMeasurePolicy : KomposSingleMeasurePolicy {
    override fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints
    ): KomposMeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

interface KomposMeasureScope : KomposDensity {
    fun layout(
        width: Int,
        height: Int,
        placement: KomposPlaceable.PlaceableScope.() -> Unit
    ): KomposMeasureResult
}

class KomposMeasureScopeImpl(
    density: KomposDensity
) : KomposMeasureScope, KomposDensity by density {

    override fun layout(
        width: Int,
        height: Int,
        placement: KomposPlaceable.PlaceableScope.() -> Unit
    ): KomposMeasureResult {
        return KomposMeasureResultImpl(
            width = width,
            height = height,
            placement = placement,
        )
    }
}

interface KomposMeasureResult {
    val width: Int
    val height: Int
    val offset: KomposPosition
    fun placeChildren(x: Int, y: Int)
}

val KomposMeasureResult.size: KomposSize
    inline get() = KomposSize.create(width, height)

class KomposMeasureResultImpl(
    override val width: Int,
    override val height: Int,
    private val placement: KomposPlaceable.PlaceableScope.() -> Unit
) : KomposMeasureResult, KomposPlaceable.PlaceableScope {

    override var offset: KomposPosition = KomposPosition.Zero

    override fun placeChildren(x: Int, y: Int) {
        offset = KomposPosition.create(x, y)
        placement()
    }

    override fun KomposPlaceable.place(x: Int, y: Int) {
        placeAt(x = x + offset.x, y = y + offset.y)
    }
}

fun Komposifier.layout(measure: KomposSingleMeasurePolicy): Komposifier {
    return then(MeasureKomposifier(measure))
}

private class MeasureKomposifier(
    private val delegatedMeasurePolicy: KomposSingleMeasurePolicy
) : Komposifier {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return KomposMeasurePolicyVisualizer(
            delegate = outer,
            newMeasurePolicy = delegatedMeasurePolicy
        )
    }

    override fun toString(): String = "MeasureKomposifier"
}