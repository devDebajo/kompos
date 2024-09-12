package ru.debajo.kompos.komposifier

import ru.debajo.kompos.KDp
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.layout

fun Komposifier.size(width: KDp, height: KDp = width): Komposifier {
    return layout { measurable, _ ->
        val placeable = measurable.measure(
            KomposConstraints.exact(width.roundToPx(), height.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

fun Komposifier.width(width: KDp): Komposifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(minWidth = width.roundToPx(), maxWidth = width.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

fun Komposifier.height(height: KDp): Komposifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(minHeight = height.roundToPx(), maxHeight = height.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}
