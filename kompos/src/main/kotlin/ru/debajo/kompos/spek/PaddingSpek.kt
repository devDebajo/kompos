package ru.debajo.kompos.spek

import ru.debajo.kompos.KDp
import ru.debajo.kompos.constrainHeight
import ru.debajo.kompos.constrainWidth
import ru.debajo.kompos.kdp
import ru.debajo.kompos.layout
import ru.debajo.kompos.offset

fun Spek.padding(
    left: KDp = 0.kdp,
    top: KDp = 0.kdp,
    right: KDp = 0.kdp,
    bottom: KDp = 0.kdp,
): Spek {
    return layout { measurable, constraints ->
        val horizontal = left.roundToPx() + right.roundToPx()
        val vertical = top.roundToPx() + bottom.roundToPx()

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        val width = constraints.constrainWidth(placeable.size.width + horizontal)
        val height = constraints.constrainHeight(placeable.size.height + vertical)
        layout(width, height) {
            placeable.place(left.roundToPx(), top.roundToPx())
        }
    }
}

fun Spek.padding(
    vertical: KDp = 0.kdp,
    horizontal: KDp = 0.kdp,
): Spek {
    return padding(
        left = horizontal,
        top = vertical,
        right = horizontal,
        bottom = vertical
    )
}

fun Spek.padding(all: KDp): Spek {
    return padding(left = all, top = all, right = all, bottom = all)
}
