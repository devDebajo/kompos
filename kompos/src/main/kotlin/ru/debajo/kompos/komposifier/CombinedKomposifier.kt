package ru.debajo.kompos.komposifier

internal class CombinedKomposifier(
    private val outerKomposifier: Komposifier,
    private val innerKomposifier: Komposifier,
) : Komposifier {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return outerKomposifier.createVisualizer(
            innerKomposifier.createVisualizer(outer)
        )
    }

    override fun toString(): String = "[$outerKomposifier, $innerKomposifier]"
}
