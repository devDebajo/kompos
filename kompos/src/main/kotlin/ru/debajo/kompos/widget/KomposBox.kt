package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.constrainHeight
import ru.debajo.kompos.constrainWidth
import ru.debajo.kompos.layout
import ru.debajo.kompos.spek.Spek
import kotlin.math.roundToInt

fun KomposScope.box(
    spek: Spek = Spek,
    contentVerticalAlignment: KomposAlignment = KomposAlignment.Start,
    contentHorizontalAlignment: KomposAlignment = KomposAlignment.Start,
    content: KomposScope.() -> Unit = {},
) {
    layout(
        spek = spek,
        content = content,
        name = "box",
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val width = constraints.constrainWidth(placeables.maxOfOrNull { it.size.width } ?: 0)
        val height = constraints.constrainHeight(placeables.maxOfOrNull { it.size.height } ?: 0)
        layout(width, height) {
            for (placeable in placeables) {
                val x = calculateOffset(width, placeable.size.width, contentHorizontalAlignment)
                val y = calculateOffset(height, placeable.size.height, contentVerticalAlignment)
                placeable.place(x = x, y = y)
            }
        }
    }
}

enum class KomposAlignment {
    Start, Center, End
}

private fun calculateOffset(
    availableSpace: Int,
    placeableSize: Int,
    alignment: KomposAlignment,
): Int {
    return when (alignment) {
        KomposAlignment.Start -> 0
        KomposAlignment.Center -> ((availableSpace - placeableSize) / 2f).roundToInt()
        KomposAlignment.End -> availableSpace - placeableSize
    }
}
