package ru.debajo.kompos.komposifier

import ru.debajo.kompos.KomposConstraints
import ru.debajo.kompos.layout

fun Komposifier.fillMaxWidth(): Komposifier {
    return fillMaxMeasurement(FillDirection.Horizontal)
}

fun Komposifier.fillMaxHeight(): Komposifier {
    return fillMaxMeasurement(FillDirection.Vertical)
}

fun Komposifier.fillMaxSize(): Komposifier {
    return fillMaxMeasurement(FillDirection.Both)
}

private fun Komposifier.fillMaxMeasurement(direction: FillDirection): Komposifier {
    return layout { measurable, constraints ->
        val minWidth: Int
        val maxWidth: Int
        if (constraints.hasBoundedWidth && direction != FillDirection.Vertical) {
            val width = constraints.maxWidth
                .coerceIn(constraints.minWidth, constraints.maxWidth)
            minWidth = width
            maxWidth = width
        } else {
            minWidth = constraints.minWidth
            maxWidth = constraints.maxWidth
        }
        val minHeight: Int
        val maxHeight: Int
        if (constraints.hasBoundedHeight && direction != FillDirection.Horizontal) {
            val height = constraints.maxHeight
                .coerceIn(constraints.minHeight, constraints.maxHeight)
            minHeight = height
            maxHeight = height
        } else {
            minHeight = constraints.minHeight
            maxHeight = constraints.maxHeight
        }
        val placeable = measurable.measure(
            KomposConstraints(
                minWidth = minWidth,
                maxWidth = maxWidth,
                minHeight = minHeight,
                maxHeight = maxHeight
            )
        )

        layout(placeable.size.width, placeable.size.height) {
            placeable.place()
        }
    }
}

private enum class FillDirection {
    Vertical, Horizontal, Both
}
