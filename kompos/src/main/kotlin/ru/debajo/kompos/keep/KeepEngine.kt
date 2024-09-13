package ru.debajo.kompos.keep

import ru.debajo.kompos.KomposScope

fun <T> KomposScope.keep(block: () -> T): T {
    TODO()
}

fun <T> KomposScope.keep(key: Any?, block: () -> T): T {
    TODO()
}

fun <T> KomposScope.keep(key1: Any?, key2: Any?, block: () -> T): T {
    TODO()
}

fun <T> KomposScope.keep(vararg keys: Any?, block: () -> T): T {
    TODO()
}
