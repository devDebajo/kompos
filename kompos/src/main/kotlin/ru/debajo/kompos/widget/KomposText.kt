package ru.debajo.kompos.widget

import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import ru.debajo.kompos.KSp
import ru.debajo.kompos.Kolor
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposRenderScope
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.ksp
import ru.debajo.kompos.layout
import ru.debajo.kompos.node.DefaultKomposMeasurePolicy
import ru.debajo.kompos.node.KomposMeasurable
import ru.debajo.kompos.node.KomposMeasureResult
import ru.debajo.kompos.node.KomposMeasureScope
import ru.debajo.kompos.spek.KomposNodeVisualizer
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.then
import kotlin.math.min
import kotlin.math.roundToInt

fun KomposScope.text(
    text: String,
    color: Kolor = Kolor.Black,
    textSize: KSp = 14.ksp,
    spek: Spek = Spek,
) {
    layout(
        name = "text",
        spek = spek.then(TextSpek(text, color, textSize.toPx())),
        content = {},
        measurePolicy = DefaultKomposMeasurePolicy
    )
}

private class TextSpek(
    private val text: String,
    private val color: Kolor,
    private val textSizePx: Float,
) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outer.then(
            TextKomposVisualizer(
                text = text,
                color = color,
                textSizePx = textSizePx,
            )
        )
    }

    override fun toString(): String = "TextSpek"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextSpek

        if (text != other.text) return false
        if (color != other.color) return false
        if (textSizePx != other.textSizePx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + textSizePx.hashCode()
        return result
    }
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
