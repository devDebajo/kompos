package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.layout
import ru.debajo.kompos.spek.Spek

fun KomposScope.column(
    spek: Spek = Spek,
    content: KomposScope.() -> Unit,
) {
    layout(
        spek = spek,
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
