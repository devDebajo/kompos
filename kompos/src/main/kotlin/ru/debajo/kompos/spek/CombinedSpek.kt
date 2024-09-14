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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CombinedSpek

        if (outerSpek != other.outerSpek) return false
        if (innerSpek != other.innerSpek) return false

        return true
    }

    override fun hashCode(): Int {
        var result = outerSpek.hashCode()
        result = 31 * result + innerSpek.hashCode()
        return result
    }
}
