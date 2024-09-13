package ru.debajo.kompos.spek

internal class CombinedSpek(
    private val outerSpek: Spek,
    private val innerSpek: Spek,
) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outerSpek.createVisualizer(
            innerSpek.createVisualizer(outer)
        )
    }

    override fun toString(): String = "[$outerSpek, $innerSpek]"
}
