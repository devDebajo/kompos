package ru.debajo.kompos.komposifier

import ru.debajo.kompos.KDp
import ru.debajo.kompos.constrainHeight
import ru.debajo.kompos.constrainWidth
import ru.debajo.kompos.kdp
import ru.debajo.kompos.layout
import ru.debajo.kompos.offset

fun Komposifier.padding(
    left: KDp = 0.kdp,
    top: KDp = 0.kdp,
    right: KDp = 0.kdp,
    bottom: KDp = 0.kdp,
): Komposifier {
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

fun Komposifier.padding(
    vertical: KDp = 0.kdp,
    horizontal: KDp = 0.kdp,
): Komposifier {
    return padding(
        left = horizontal,
        top = vertical,
        right = horizontal,
        bottom = vertical
    )
}

fun Komposifier.padding(all: KDp): Komposifier {
    return padding(left = all, top = all, right = all, bottom = all)
}
