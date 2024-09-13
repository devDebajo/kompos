package ru.debajo.kompos.spek

interface Spek {
    fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer

    companion object : Spek {
        override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer = outer
        override fun toString(): String = "Spek"
    }
}

fun Spek.then(inner: Spek): Spek {
    return CombinedSpek(outerSpek = this, innerSpek = inner)
}
