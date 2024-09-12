package ru.debajo.kompos

@JvmInline
value class KomposSize(private val packed: Long) {
    val width: Int
        get() = unpackInt1(packed)

    val height: Int
        get() = unpackInt2(packed)

    override fun toString(): String {
        return "KomposSize(width=$width, height=$height)"
    }

    companion object {
        fun create(width: Int, height: Int): KomposSize {
            return KomposSize(packInts(width, height))
        }

        val Zero: KomposSize = create(0, 0)
    }
}

@JvmInline
value class KomposPosition(private val packed: Long) {
    val x: Int
        get() = unpackInt1(packed)

    val y: Int
        get() = unpackInt2(packed)

    operator fun component1(): Int = x

    operator fun component2(): Int = y

    override fun toString(): String {
        return "KomposPosition(x=$x, y=$y)"
    }

    companion object {
        fun create(x: Int, y: Int): KomposPosition {
            return KomposPosition(packInts(x, y))
        }

        val Zero: KomposPosition = create(0, 0)
    }
}

@JvmInline
value class KDp(val value: Float)

val Float.kdp: KDp
    get() = KDp(this)

val Int.kdp: KDp
    get() = toFloat().kdp

@JvmInline
value class KSp(val value: Float)

val Float.ksp: KSp
    get() = KSp(this)

val Int.ksp: KSp
    get() = toFloat().ksp

