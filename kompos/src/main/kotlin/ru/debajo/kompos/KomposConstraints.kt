package ru.debajo.kompos

// TODO сделать пул этих штук
data class KomposConstraints(
    val minWidth: Int = 0,
    val minHeight: Int = 0,
    val maxWidth: Int = Int.MAX_VALUE,
    val maxHeight: Int = Int.MAX_VALUE,
) {
    val isWidthUnspecified: Boolean
        get() = maxWidth == Int.MAX_VALUE

    val isWidthExact: Boolean
        get() = !isWidthUnspecified && minWidth == maxWidth

    val isHeightUnspecified: Boolean
        get() = maxHeight == Int.MAX_VALUE

    val isHeightExact: Boolean
        get() = !isHeightUnspecified && minHeight == maxHeight

    val hasBoundedWidth: Boolean
        get() = !isWidthUnspecified

    val hasBoundedHeight: Boolean
        get() = !isHeightUnspecified

    companion object {
        fun exact(width: Int, height: Int): KomposConstraints {
            return KomposConstraints(
                minWidth = width,
                maxWidth = width,
                minHeight = height,
                maxHeight = height,
            )
        }
    }
}

fun KomposConstraints.constrainWidth(width: Int): Int = width.coerceIn(minWidth, maxWidth)

fun KomposConstraints.constrainHeight(height: Int): Int = height.coerceIn(minHeight, maxHeight)

fun KomposConstraints.offset(horizontal: Int = 0, vertical: Int = 0): KomposConstraints {
    return KomposConstraints(
        minWidth = (minWidth + horizontal).coerceAtLeast(0),
        maxWidth = addMaxWithMinimum(maxWidth, horizontal),
        minHeight = (minHeight + vertical).coerceAtLeast(0),
        maxHeight = addMaxWithMinimum(maxHeight, vertical)
    )
}

private fun addMaxWithMinimum(max: Int, value: Int): Int {
    return if (max == Int.MAX_VALUE) {
        max
    } else {
        (max + value).coerceAtLeast(0)
    }
}
