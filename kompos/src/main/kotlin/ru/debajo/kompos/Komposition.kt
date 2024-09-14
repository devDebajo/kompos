package ru.debajo.kompos

import android.graphics.Canvas
import android.util.Log
import ru.debajo.kompos.node.KomposNode

class Komposition(
    override val currentKomposer: Komposer,
    private val renderSizeEffect: KomposRenderSizeEffect,
) : KomposScope,
    KomposDensity by currentKomposer.density,
    KomposRenderSizeEffect by renderSizeEffect {

    private var node: KomposNode? = null

    var content: KomposScope.() -> Unit = {}

    init {
        currentKomposer.onChangedListener = {
            rekompose()
        }
    }

    fun rekompose() {
        node = null
        node = ensureNode()
        onSizeChanged()
    }

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

    fun printTree() {
        Log.d("yopta", ensureNode().format(0))
    }

    private fun ensureNode(): KomposNode {
        val node = node
        if (node != null) {
            return node
        }
        currentKomposer.startKomposing()
        content()
        val newRootNode = currentKomposer.buildTree()
        currentKomposer.endKomposing()
        this.node = newRootNode
        return newRootNode
    }
}
