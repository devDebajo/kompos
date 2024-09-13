package ru.debajo.kompos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import ru.debajo.kompos.komposifier.Komposifier
import kotlin.math.roundToInt

interface KomposScope : KomposDensity {

    fun addNode(node: KomposNode)

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
        komposition!!.printTree()
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
    private val node: KomposNode = createNode("root", Komposifier)

    override fun addNode(node: KomposNode) {
        this.node.addChild(node)
    }

    fun measure(constraints: KomposConstraints): KomposSize {
        val placeable = node.asMeasurable().measure(constraints)
        placeable.placeAt(0, 0)
        return placeable.size
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        node.draw(canvas, KomposSize.create(width, height))
    }

    fun onTouch(touchEvent: KomposTouchEvent): Boolean {
        return node.onTouch(touchEvent)
    }

    fun printTree() {
        Log.d("yopta", node.format(0))
    }
}

class KomposScopeImpl(
    name: String,
    private val parentScope: KomposScope,
    komposifier: Komposifier
) : KomposScope, KomposDensity by parentScope {

    private val node: KomposNode = createNode(name, komposifier)

    init {
        parentScope.addNode(node)
    }

    override fun addNode(node: KomposNode) {
        this.node.addChild(node)
    }

    override val currentKomposer: Komposer
        get() = parentScope.currentKomposer

    fun registerMeasurePolicy(measurePolicy: KomposMeasurePolicy) {
        node.childMeasurePolicy = measurePolicy
    }
}
