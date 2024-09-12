package ru.debajo.kompos

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

@JvmInline
value class Kolor(val argb: Int) {

    fun alpha(alpha: Float): Kolor {
        val newArgb =
            ColorUtils.setAlphaComponent(argb, (255 * alpha).roundToInt().coerceIn(0, 255))
        return Kolor(newArgb)
    }

    companion object {
        val Black: Kolor = Kolor(Color.BLACK)
        val DkGray: Kolor = Kolor(Color.DKGRAY)
        val Gray: Kolor = Kolor(Color.GRAY)
        val LtGray: Kolor = Kolor(Color.LTGRAY)
        val White: Kolor = Kolor(Color.WHITE)
        val Red: Kolor = Kolor(Color.RED)
        val Green: Kolor = Kolor(Color.GREEN)
        val Blue: Kolor = Kolor(Color.BLUE)
        val YELLOW: Kolor = Kolor(Color.YELLOW)
        val Cyan: Kolor = Kolor(Color.CYAN)
        val Magenta: Kolor = Kolor(Color.MAGENTA)
        val Transparent: Kolor = Kolor(Color.TRANSPARENT)
    }
}

@Suppress("SpellCheckingInspection")
fun Kolor(hex: String): Kolor {
    return Kolor(Color.parseColor(hex))
}
