package ru.debajo.kompos.keep

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.node.KomposCallKey

fun <T> KomposScope.keep(block: () -> T): T {
    return keep(Unit, block)
}

fun <T> KomposScope.keep(vararg keys: Any?, block: () -> T): T {
    return keep(key = keys, block = block)
}

fun <T> KomposScope.keep(key: Any, block: () -> T): T {
    val currentCallKey = KomposCallKey.current()
    return currentKomposer.keep(currentCallKey, key, block)
}

// TODO реализовать нормально
interface KeepObserver {
    fun onKeep() = Unit

    fun onLost() = Unit
}
