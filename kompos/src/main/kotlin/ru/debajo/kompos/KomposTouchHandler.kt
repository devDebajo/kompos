package ru.debajo.kompos

import ru.debajo.kompos.spek.KomposMeasureResultVisualizer
import ru.debajo.kompos.spek.KomposNodeVisualizer
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.then
import kotlin.math.pow
import kotlin.math.sqrt

interface KomposTouchHandler {
    fun onTouch(event: KomposTouchEvent): Boolean = false
}

object EmptyKomposTouchHandler : KomposTouchHandler

fun Spek.clickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Spek {
    if (!enabled) {
        return this
    }

    var downEvent: KomposTouchEvent? = null
    return touchEvents { event ->
        val localDownEvent = downEvent
        if (localDownEvent == null) {
            if (event.type == KomposTouchType.Down) {
                downEvent = event
                true
            } else {
                downEvent = null
                false
            }
        } else {
            when (event.type) {
                KomposTouchType.Up -> {
                    if (event.distanceTo(localDownEvent) <= 30) {
                        onClick()
                        downEvent = null
                        true
                    } else {
                        downEvent = null
                        false
                    }
                }

                KomposTouchType.Cancel -> {
                    downEvent = null
                    true
                }

                KomposTouchType.Down -> false
                KomposTouchType.Move -> true
            }
        }
    }
}

fun Spek.touchEvents(onTouchEvent: (KomposTouchEvent) -> Boolean): Spek {
    return then(TouchEventsSpek(onTouchEvent))
}

private class TouchEventsSpek(
    private val onTouchEvent: (KomposTouchEvent) -> Boolean
) : Spek {
    override fun createVisualizer(outer: KomposNodeVisualizer): KomposNodeVisualizer {
        return TouchEventsVisualizer(outer, onTouchEvent)
    }
}

private class TouchEventsVisualizer(
    private val delegate: KomposNodeVisualizer,
    private val onTouchEvent: (KomposTouchEvent) -> Boolean
) : KomposMeasureResultVisualizer(delegate), KomposRenderAction by delegate {

    override fun onTouch(event: KomposTouchEvent): Boolean {
        return if (event.type == KomposTouchType.Down) {
            if (isHit(event)) {
                onTouchEvent(event)
            } else {
                false
            }
        } else {
            onTouchEvent(event)
        }
    }

    private fun isHit(event: KomposTouchEvent): Boolean {
        val (left, top) = lastPosition
        val bottom = top + lastSize.height
        val right = left + lastSize.width
        return event.x in left..right && event.y in top..bottom
    }
}

data class KomposTouchEvent(
    val x: Int,
    val y: Int,
    val time: Long,
    val type: KomposTouchType
) {
    fun distanceTo(downEvent: KomposTouchEvent): Float {
        return sqrt((downEvent.x - x.toFloat()).pow(2) + (downEvent.y - y.toFloat()).pow(2))
    }
}

enum class KomposTouchType {
    Down, Up, Cancel, Move
}
