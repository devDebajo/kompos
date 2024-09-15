package ru.debajo.kompos.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.keep.KeepObserver
import ru.debajo.kompos.keep.keep

fun KomposScope.onLaunch(key: Any, block: suspend CoroutineScope.() -> Unit) {
    keep(key) {
        OnLaunch(
            coroutineScope = currentKomposer.coroutineScope,
            block = block,
        )
    }
}

fun KomposScope.onLaunch(vararg keys: Any?, block: suspend CoroutineScope.() -> Unit) {
    onLaunch(key = keys.toList(), block = block)
}

fun KomposScope.onLost(
    key: Any,
    block: OnLostScope.() -> OnLostResult,
) {
    keep(key) { OnLost(block) }
}

object OnLostScope {
    fun onDispose(dispose: () -> Unit): OnLostResult {
        return object : OnLostResult {
            override fun dispose() {
                dispose()
            }
        }
    }
}

interface OnLostResult {
    fun dispose()
}

private class OnLaunch(
    coroutineScope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> Unit
) : KeepObserver {
    private val childCoroutineScope: CoroutineScope = coroutineScope.child()
    private var job: Job? = null

    init {
        // TODO переделать нормально
        onKeep()
    }

    override fun onKeep() {
        job?.cancel()
        job = childCoroutineScope.launch { block() }
    }

    override fun onLost() {
        job?.cancel()
    }
}

private class OnLost(private val block: OnLostScope.() -> OnLostResult) : KeepObserver {
    private var result: OnLostResult? = null

    override fun onKeep() {
        result = OnLostScope.block()
    }

    override fun onLost() {
        result?.dispose()
        result = null
    }
}
