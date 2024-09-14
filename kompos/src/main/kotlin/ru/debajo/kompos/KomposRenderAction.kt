package ru.debajo.kompos

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import ru.debajo.kompos.spek.KomposNodeVisualizer
import ru.debajo.kompos.spek.KomposRenderActionVisualizer
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.then
import java.lang.ref.WeakReference

fun interface KomposRenderAction {
    fun KomposRenderScope.render()
}

interface KomposRenderScope : KomposDensity {
    val canvas: Canvas

    val size: KomposSize

    fun drawContent()
}

class MutableKomposRenderScope(
    private val drawContentDelegate: KomposRenderScope.() -> Unit,
) : KomposRenderScope {

    private var canvasRef: WeakReference<Canvas>? = null
    private var densityRef: WeakReference<KomposDensity>? = null

    fun configure(canvas: Canvas, density: KomposDensity) {
        canvasRef = saveToRef(canvasRef, canvas)
        densityRef = saveToRef(densityRef, density)
    }

    override val canvas: Canvas
        get() = canvasRef?.get()!!

    override var size: KomposSize = KomposSize.Zero

    override fun drawContent() {
        drawContentDelegate()
    }

    override fun KDp.toPx(): Float {
        return with(densityRef?.get()!!) {
            toPx()
        }
    }

    override fun KSp.toPx(): Float {
        return with(densityRef?.get()!!) {
            toPx()
        }
    }

    override fun getDrawable(id: Int): Drawable {
        return densityRef?.get()?.getDrawable(id)!!
    }

    private fun <T> saveToRef(current: WeakReference<T>?, value: T): WeakReference<T> {
        if (current != null && current.get() === value) {
            return current
        }
        current?.clear()
        return WeakReference(value)
    }
}

object EmptyKomposRenderAction : KomposRenderAction {
    override fun KomposRenderScope.render() = Unit
}

fun Spek.clip(shape: KomposShape): Spek {
    return drawWithContent {
        val path = with(shape) { createPath(size) }
        canvas.withClip(path) {
            drawContent()
        }
    }
}

fun Spek.background(color: Kolor): Spek {
    return drawWithContent {
        val paint = Paint()
        paint.color = color.argb
        canvas.drawRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), paint)
        drawContent()
    }
}

fun Spek.drawWithContent(draw: KomposRenderAction): Spek {
    return then(RenderSpek(draw))
}

private class RenderSpek(
    private val delegatedRenderAction: KomposRenderAction,
) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return KomposRenderActionVisualizer(
            delegate = outer,
            newRenderAction = delegatedRenderAction
        )
    }

    override fun toString(): String = "RenderSpek"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RenderSpek

        return delegatedRenderAction == other.delegatedRenderAction
    }

    override fun hashCode(): Int = delegatedRenderAction.hashCode()
}
