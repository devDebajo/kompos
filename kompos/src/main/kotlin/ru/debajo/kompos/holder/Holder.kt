package ru.debajo.kompos.holder

import ru.debajo.kompos.GlobalKomposer
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

    private val accessingSet: MutableSet<String> = HashSet()

    private var backingField: T = initialValue

    override var value: T
        get() {
            val currentComposingId = GlobalKomposer.currentComposingId
            if (currentComposingId != null) {
                accessingSet.add(currentComposingId)
            }
            return backingField
        }
        set(value) {
            backingField = value
            for (komposerId in accessingSet) {
                GlobalKomposer.notifyChanged(komposerId)
            }
        }

    override fun toString(): String {
        return "MutableHolderImpl(value=$backingField)"
    }
}
