package ru.debajo.kompos.widget

import android.graphics.drawable.Drawable
import ru.debajo.kompos.DefaultKomposMeasurePolicy
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposMeasurable
import ru.debajo.kompos.KomposMeasureResult
import ru.debajo.kompos.KomposMeasureScope
import ru.debajo.kompos.KomposRenderScope
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.constrainHeight
import ru.debajo.kompos.constrainWidth
import ru.debajo.kompos.komposifier.KomposNodeVisualizer
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.komposifier.then
import ru.debajo.kompos.layout

fun KomposScope.drawable(
    id: Int,
    komposifier: Komposifier = Komposifier,
) {
    layout(
        komposifier = komposifier.then(DrawableKomposifier(getDrawable(id))),
        name = "drawable",
        content = {},
        measurePolicy = DefaultKomposMeasurePolicy
    )
}

private class DrawableKomposifier(private val drawable: Drawable) : Komposifier {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outer.then(DrawableVisualizer(drawable))
    }
}

private class DrawableVisualizer(private val drawable: Drawable) : KomposNodeVisualizer {
    override fun KomposMeasureScope.measure(
        measurable: KomposMeasurable,
        constraints: KomposConstraints
    ): KomposMeasureResult {
        val width = constraints.constrainWidth(drawable.intrinsicWidth)
        val height = constraints.constrainHeight(drawable.intrinsicWidth)

        val placeable = measurable.measure(KomposConstraints.exact(width, height))
        drawable.setBounds(0, 0, width, height)
        return layout(width, height) {
            placeable.place()
        }
    }

    override fun KomposRenderScope.render() {
        drawable.draw(canvas)
    }
}
