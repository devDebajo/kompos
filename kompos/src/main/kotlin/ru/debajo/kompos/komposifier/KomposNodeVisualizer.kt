package ru.debajo.kompos.komposifier

import androidx.core.graphics.withTranslation
import ru.debajo.kompos.DefaultKomposSingleMeasurePolicy
import ru.debajo.kompos.EmptyKomposRenderAction
import ru.debajo.kompos.EmptyKomposTouchHandler
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposMeasurable
import ru.debajo.kompos.KomposMeasureResult
import ru.debajo.kompos.KomposMeasureScope
import ru.debajo.kompos.KomposPosition
import ru.debajo.kompos.KomposRenderAction
import ru.debajo.kompos.KomposRenderScope
import ru.debajo.kompos.KomposSingleMeasurePolicy
import ru.debajo.kompos.KomposSize
import ru.debajo.kompos.KomposTouchHandler
import ru.debajo.kompos.MutableKomposRenderScope
import ru.debajo.kompos.asMeasurable
import ru.debajo.kompos.size

interface KomposNodeVisualizer : KomposSingleMeasurePolicy, KomposRenderAction, KomposTouchHandler

fun KomposNodeVisualizer.then(inner: KomposNodeVisualizer): KomposNodeVisualizer {
    return KomposRenderActionVisualizer(
        delegate = KomposMeasurePolicyVisualizer(delegate = this, newMeasurePolicy = inner),
        newRenderAction = inner,
    )
}

object DefaultKomposNodeVisualizer : KomposNodeVisualizer,
    KomposSingleMeasurePolicy by DefaultKomposSingleMeasurePolicy,
    KomposRenderAction by EmptyKomposRenderAction,
    KomposTouchHandler by EmptyKomposTouchHandler

class KomposMeasurePolicyVisualizer(
    private val delegate: KomposNodeVisualizer,
    private val newMeasurePolicy: KomposSingleMeasurePolicy,
) : KomposNodeVisualizer by delegate {
    override fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints
    ): KomposMeasureResult {
        return with(newMeasurePolicy) {
            measure(delegate.asMeasurable(measurable, this@measure), constraints)
        }
    }
}

abstract class KomposMeasureResultVisualizer(
    private val delegate: KomposNodeVisualizer,
) : KomposNodeVisualizer {
    private var lastMeasureResult: KomposMeasureResult? = null

    protected val lastSize: KomposSize
        get() = lastMeasureResult?.size ?: KomposSize.Zero

    protected val lastPosition: KomposPosition
        get() = lastMeasureResult?.offset ?: KomposPosition.Zero

    final override fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints
    ): KomposMeasureResult {
        return with(delegate) { measure(measurable, constraints) }
            .also { lastMeasureResult = it }
    }
}

class KomposRenderActionVisualizer(
    private val delegate: KomposNodeVisualizer,
    private val newRenderAction: KomposRenderAction,
) : KomposMeasureResultVisualizer(delegate), KomposTouchHandler by delegate {

    private val renderScope: MutableKomposRenderScope = MutableKomposRenderScope {
        val lastPosition = lastPosition
        canvas.withTranslation(-lastPosition.x.toFloat(), -lastPosition.y.toFloat()) {
            with(delegate) {
                render()
            }
        }
    }

    override fun KomposRenderScope.render() {
        renderScope.configure(canvas, this)
        renderScope.size = lastSize
        with(renderScope) {
            val lastPosition = lastPosition
            canvas.withTranslation(lastPosition.x.toFloat(), lastPosition.y.toFloat()) {
                with(newRenderAction) {
                    render()
                }
            }
        }
    }
}
