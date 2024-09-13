package ru.debajo.kompos.holder

import kotlin.reflect.KProperty

interface Holder<T> {
    val value: T
}

interface MutableHolder<T> : Holder<T> {
    override var value: T
}

fun <T> mutableHolderOf(initialValue: T): MutableHolder<T> {
    return MutableHolderImpl(initialValue)
}

operator fun <T> Holder<T>.getValue(thisObj: Any?, property: KProperty<*>): T = value

operator fun <T> MutableHolder<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

private class MutableHolderImpl<T>(initialValue: T) : MutableHolder<T> {
    override var value: T
        get() = TODO("Not yet implemented")
        set(value) {}
}
