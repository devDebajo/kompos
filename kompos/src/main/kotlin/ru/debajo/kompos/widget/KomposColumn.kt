package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.layout

fun KomposScope.column(
    komposifier: Komposifier = Komposifier,
    content: KomposScope.() -> Unit,
) {
    layout(
        komposifier = komposifier,
        content = content,
        name = "column",
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        val width = placeables.maxOf { it.size.width }
        val height = placeables.sumOf { it.size.height }

        layout(width, height) {
            var top = 0
            for (placeable in placeables) {
                placeable.place(y = top)
                top += placeable.size.height
            }
        }
    }
}
