package ru.debajo.kompos

import android.graphics.Path
import android.graphics.RectF

interface KomposShape {
    fun KomposDensity.createPath(size: KomposSize): Path
}

data class KomposRoundedCornerShape(
    val topLeft: KDp = 0.kdp,
    val topRight: KDp = 0.kdp,
    val bottomLeft: KDp = 0.kdp,
    val bottomRight: KDp = 0.kdp,
) : KomposShape {

    constructor(all: KDp) : this(all, all, all, all)

    private val radii: FloatArray = FloatArray(8)
    private val path: Path = Path()
    private val rect: RectF = RectF()

    override fun KomposDensity.createPath(size: KomposSize): Path {
        radii[0] = topLeft.toPx()
        radii[1] = topLeft.toPx()
        radii[2] = topRight.toPx()
        radii[3] = topRight.toPx()
        radii[4] = bottomRight.toPx()
        radii[5] = bottomRight.toPx()
        radii[6] = bottomLeft.toPx()
        radii[7] = bottomLeft.toPx()

        rect.set(0f, 0f, size.width.toFloat(), size.height.toFloat())
        path.reset()
        path.addRoundRect(rect, radii, Path.Direction.CW)
        return path
    }
}
