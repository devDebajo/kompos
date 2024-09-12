package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.layout

fun KomposScope.row(
    komposifier: Komposifier = Komposifier,
    content: KomposScope.() -> Unit,
) {
    layout(
        komposifier = komposifier,
        content = content,
        name = "row",
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        val width = placeables.sumOf { it.size.width }
        val height = placeables.maxOf { it.size.height }

        layout(width, height) {
            var left = 0
            for (placeable in placeables) {
                placeable.place(x = left)
                left += placeable.size.width
            }
        }
    }
}
