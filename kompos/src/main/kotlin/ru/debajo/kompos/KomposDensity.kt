package ru.debajo.kompos

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

interface KomposDensity {
    fun KDp.toPx(): Float

    fun KDp.roundToPx(): Int = toPx().roundToInt()

    fun KSp.toPx(): Float

    fun KSp.roundToPx(): Int = toPx().roundToInt()

    fun getDrawable(id: Int): Drawable
}

fun KDp.toPx(density: KomposDensity): Float {
    return with(density) { toPx() }
}

fun KSp.toPx(density: KomposDensity): Float {
    return with(density) { toPx() }
}

object DefaultKomposDensity : KomposDensity {
    override fun KDp.toPx(): Float = value

    override fun KSp.toPx(): Float = value

    override fun getDrawable(id: Int): Drawable {
        error("DefaultKomposDensity could not get drawable")
    }
}

class KomposContextDensity(private val context: Context) : KomposDensity {
    override fun KDp.toPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.value,
            context.resources.displayMetrics
        )
    }

    override fun KSp.toPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.value,
            context.resources.displayMetrics
        )
    }

    override fun getDrawable(id: Int): Drawable {
        return ContextCompat.getDrawable(context, id)!!
    }
}
