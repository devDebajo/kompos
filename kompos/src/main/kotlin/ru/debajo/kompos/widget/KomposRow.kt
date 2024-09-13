package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.layout
import ru.debajo.kompos.spek.Spek

fun KomposScope.row(
    spek: Spek = Spek,
    content: KomposScope.() -> Unit,
) {
    layout(
        spek = spek,
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
