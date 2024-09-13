package ru.debajo.kompos.node

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import ru.debajo.kompos.DefaultKomposDensity
import ru.debajo.kompos.KDp
import ru.debajo.kompos.KSp
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposDensity
import ru.debajo.kompos.KomposPosition
import ru.debajo.kompos.KomposSize
import ru.debajo.kompos.KomposTouchEvent
import ru.debajo.kompos.MutableKomposRenderScope
import ru.debajo.kompos.spek.DefaultKomposNodeVisualizer
import ru.debajo.kompos.spek.KomposMeasurePolicyVisualizer
import ru.debajo.kompos.spek.KomposNodeVisualizer
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.then

internal class KomposNode : KomposDensity {
    private var density: KomposDensity = DefaultKomposDensity

    var name: String = ""
        private set

    var key: KomposNodeKey = KomposNodeKey.Empty
        private set

    var spek: Spek = Spek
        set(value) {
            field = value
            visualizer = spek.createVisualizer(DefaultKomposNodeVisualizer)
        }
    var childMeasurePolicy: KomposMeasurePolicy = DefaultKomposMeasurePolicy
    private var visualizer: KomposNodeVisualizer = DefaultKomposNodeVisualizer

    private val nestedNodes: MutableList<KomposNode> = mutableListOf()

    private val measureScope: KomposMeasureScopeImpl = KomposMeasureScopeImpl(this)
    private val renderScope: MutableKomposRenderScope = MutableKomposRenderScope {
        with(visualizer) {
            render()
        }
        for (nestedNode in nestedNodes) {
            nestedNode.draw(canvas, size)
        }
    }

    fun inflate(density: KomposDensity, name: String, key: KomposNodeKey) {
        this.density = density
        this.name = name
        this.key = key
    }

    fun draw(canvas: Canvas, size: KomposSize) {
        renderScope.size = size
        renderScope.configure(canvas, this)
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
            visualizer.onTouch(touchEvent)
        }
    }

    fun asMeasurable(): KomposMeasurable {
        return object : KomposMeasurable {
            override fun measure(constraints: KomposConstraints): KomposPlaceable {
                val measureResult = with(visualizer) {
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

    override fun KDp.toPx(): Float {
        return with(density) { toPx() }
    }

    override fun KSp.toPx(): Float {
        return with(density) { toPx() }
    }

    override fun getDrawable(id: Int): Drawable {
        return with(density) { getDrawable(id) }
    }

    fun clear() {
        density = DefaultKomposDensity
        name = ""
        key = KomposNodeKey.Empty
        spek = Spek
        childMeasurePolicy = DefaultKomposMeasurePolicy
        nestedNodes.clear()
    }

    override fun toString(): String = "KomposNode($name)"

    fun format(depth: Int): String {
        return buildString {
            appendLine("${createIndent(depth)}KomposNode(")
            appendLine("${createIndent(depth + 1)}name = $name,")
            appendLine("${createIndent(depth + 1)}key = ${key.key},")
            appendLine("${createIndent(depth + 1)}spek = $spek,")
            if (nestedNodes.isNotEmpty()) {
                appendLine("${createIndent(depth + 1)}children = [")
                for (nestedNode in nestedNodes) {
                    appendLine("${nestedNode.format(depth + 2)},")
                }
                appendLine("${createIndent(depth + 1)}]")
            }
            append("${createIndent(depth)})")
        }
    }

    private fun createIndent(depth: Int): String {
        return "    ".repeat(depth)
    }

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

fun Spek.layout(measure: KomposSingleMeasurePolicy): Spek {
    return then(MeasureSpek(measure))
}

private class MeasureSpek(
    private val delegatedMeasurePolicy: KomposSingleMeasurePolicy
) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return KomposMeasurePolicyVisualizer(
            delegate = outer,
            newMeasurePolicy = delegatedMeasurePolicy
        )
    }

    override fun toString(): String = "MeasureSpek"
}
