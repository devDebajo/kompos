package ru.debajo.kompos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

interface KomposScope : KomposDensity {
    val currentKomposer: Komposer
}

class KomposView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var komposition: Komposition? = null

    fun setContent(content: KomposScope.() -> Unit) {
        komposition = Komposition(
            KomposContextDensity(context),
            // TODO getOrCreateComposer
            GlobalKomposer.newKomposer(),
        )

        komposition!!.content()
        requestLayout()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val komposition = komposition
        if (komposition == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var minWidth = 0
        var minHeight = 0
        var maxWidth = Int.MAX_VALUE
        var maxHeight = Int.MAX_VALUE

        when (widthMode) {
            MeasureSpec.EXACTLY -> {
                maxWidth = widthSize
                minWidth = widthSize
            }

            MeasureSpec.AT_MOST -> {
                maxWidth = widthSize
            }

            else -> Unit
        }

        when (heightMode) {
            MeasureSpec.EXACTLY -> {
                maxHeight = heightSize
                minHeight = heightSize
            }

            MeasureSpec.AT_MOST -> {
                maxHeight = heightSize
            }

            else -> Unit
        }

        val size = komposition.measure(
            KomposConstraints(
                minWidth = minWidth,
                minHeight = minHeight,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        )
        setMeasuredDimension(size.width, size.height)
    }

    override fun onDraw(canvas: Canvas) {
        val komposition = komposition
        if (komposition != null) {
            komposition.draw(canvas, measuredWidth, measuredHeight)
        } else {
            super.onDraw(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val komposition = komposition ?: return super.onTouchEvent(event)

        val touchType = when (event.action) {
            MotionEvent.ACTION_CANCEL -> KomposTouchType.Cancel
            MotionEvent.ACTION_MOVE -> KomposTouchType.Move
            MotionEvent.ACTION_DOWN -> KomposTouchType.Down
            MotionEvent.ACTION_UP -> KomposTouchType.Up
            else -> return super.onTouchEvent(event)
        }

        val touchEvent = KomposTouchEvent(
            x = event.x.roundToInt(),
            y = event.y.roundToInt(),
            time = event.eventTime,
            type = touchType
        )

        return if (komposition.onTouch(touchEvent)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }
}

class Komposition(
    density: KomposDensity,
    override val currentKomposer: Komposer,
) : KomposScope, KomposDensity by density {

    private var node: KomposNodePooled? = null

    fun measure(constraints: KomposConstraints): KomposSize {
        val placeable = ensureNode().asMeasurable().measure(constraints)
        placeable.placeAt(0, 0)
        return placeable.size
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        ensureNode().draw(canvas, KomposSize.create(width, height))
    }

    fun onTouch(touchEvent: KomposTouchEvent): Boolean {
        return ensureNode().onTouch(touchEvent)
    }

    private fun ensureNode(): KomposNodePooled {
        return node ?: currentKomposer.buildTree().also {
            node = it
        }
    }
}
