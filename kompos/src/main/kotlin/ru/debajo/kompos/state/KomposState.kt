package ru.debajo.kompos.state

interface KomposState<T> {
    val value: T
}

interface KomposMutableState<T> : KomposState<T> {
    override var value: T
}
