package ru.debajo.kompos.widget

import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import ru.debajo.kompos.DefaultKomposMeasurePolicy
import ru.debajo.kompos.KSp
import ru.debajo.kompos.Kolor
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposMeasurable
import ru.debajo.kompos.KomposMeasureResult
import ru.debajo.kompos.KomposMeasureScope
import ru.debajo.kompos.KomposRenderScope
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.komposifier.KomposNodeVisualizer
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.komposifier.then
import ru.debajo.kompos.ksp
import ru.debajo.kompos.layout
import kotlin.math.min
import kotlin.math.roundToInt

fun KomposScope.text(
    text: String,
    color: Kolor = Kolor.Black,
    textSize: KSp = 14.ksp,
    komposifier: Komposifier = Komposifier,
) {
    layout(
        name = "text",
        komposifier = komposifier.then(
            TextKomposifier(TextKomposVisualizer(text, color, textSize.toPx()))
        ),
        content = {},
        measurePolicy = DefaultKomposMeasurePolicy
    )
}

private class TextKomposifier(private val visualizer: TextKomposVisualizer) : Komposifier {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outer.then(visualizer)
    }

    override fun toString(): String = "TextKomposifier"
}

private class TextKomposVisualizer(
    private val text: String,
    private val color: Kolor,
    private val textSizePx: Float,
) : KomposNodeVisualizer {

    private val paint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@TextKomposVisualizer.color.argb
        textSize = textSizePx
    }

    private var layout: Layout? = null

    override fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints,
    ): KomposMeasureResult {
        val desiredWidth = StaticLayout.getDesiredWidth(text, 0, text.length, paint)
        val textWidth = when {
            constraints.isWidthUnspecified -> desiredWidth.roundToInt()
            constraints.isWidthExact -> constraints.maxWidth
            else -> min(desiredWidth.roundToInt(), constraints.maxWidth)
        }

        val localLayout = StaticLayout(
            text,
            0,
            text.length,
            paint,
            textWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false
        )
        layout = localLayout

        val resultHeight = if (constraints.isHeightExact) {
            constraints.maxHeight
        } else {
            localLayout.height
        }
        val placeable = measurable.measure(KomposConstraints.exact(textWidth, resultHeight))
        return layout(textWidth, resultHeight) {
            placeable.place()
        }
    }

    override fun KomposRenderScope.render() {
        layout?.draw(canvas)
    }
}
