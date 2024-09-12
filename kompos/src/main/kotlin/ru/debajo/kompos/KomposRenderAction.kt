package ru.debajo.kompos

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import ru.debajo.kompos.komposifier.KomposNodeVisualizer
import ru.debajo.kompos.komposifier.KomposRenderActionVisualizer
import ru.debajo.kompos.komposifier.Komposifier
import ru.debajo.kompos.komposifier.then
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

fun Komposifier.clip(shape: KomposShape): Komposifier {
    return drawWithContent {
        val path = with(shape) { createPath(size) }
        canvas.withClip(path) {
            drawContent()
        }
    }
}

fun Komposifier.background(color: Kolor): Komposifier {
    return drawWithContent {
        val paint = Paint()
        paint.color = color.argb
        canvas.drawRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), paint)
        drawContent()
    }
}

fun Komposifier.drawWithContent(draw: KomposRenderAction): Komposifier {
    return then(RenderKomposifier(draw))
}

private class RenderKomposifier(
    private val delegatedRenderAction: KomposRenderAction,
) : Komposifier {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return KomposRenderActionVisualizer(
            delegate = outer,
            newRenderAction = delegatedRenderAction
        )
    }

    override fun toString(): String = "RenderKomposifier"
}
