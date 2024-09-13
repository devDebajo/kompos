package ru.debajo.kompos.spek

import ru.debajo.kompos.KDp
import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.node.layout

fun Spek.size(width: KDp, height: KDp = width): Spek {
    return layout { measurable, _ ->
        val placeable = measurable.measure(
            KomposConstraints.exact(width.roundToPx(), height.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

fun Spek.width(width: KDp): Spek {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(minWidth = width.roundToPx(), maxWidth = width.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

fun Spek.height(height: KDp): Spek {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(minHeight = height.roundToPx(), maxHeight = height.roundToPx())
        )
        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}
