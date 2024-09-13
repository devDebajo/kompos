package ru.debajo.kompos.widget

import android.graphics.drawable.Drawable
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.KomposRenderScope
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.constrainHeight
import ru.debajo.kompos.constrainWidth
import ru.debajo.kompos.layout
import ru.debajo.kompos.node.DefaultKomposMeasurePolicy
import ru.debajo.kompos.node.KomposMeasurable
import ru.debajo.kompos.node.KomposMeasureResult
import ru.debajo.kompos.node.KomposMeasureScope
import ru.debajo.kompos.spek.KomposNodeVisualizer
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.then

fun KomposScope.drawable(
    id: Int,
    spek: Spek = Spek,
) {
    layout(
        spek = spek.then(DrawableSpek(getDrawable(id))),
        name = "drawable",
        content = {},
        measurePolicy = DefaultKomposMeasurePolicy
    )
}

private class DrawableSpek(private val drawable: Drawable) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outer.then(DrawableVisualizer(drawable))
    }

    override fun toString(): String = "DrawableSpek"
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
