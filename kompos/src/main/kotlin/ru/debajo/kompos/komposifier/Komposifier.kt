package ru.debajo.kompos.komposifier

interface Komposifier {
    fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer

    companion object : Komposifier {
        override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer = outer
        override fun toString(): String = "Komposifier"
    }
}

fun Komposifier.then(inner: Komposifier): Komposifier {
    return CombinedKomposifier(outerKomposifier = this, innerKomposifier = inner)
}
