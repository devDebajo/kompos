package ru.debajo.kompos.holder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ru.debajo.kompos.GlobalKomposer
import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.keep.keep
import ru.debajo.kompos.utils.onLaunch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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

fun <T> KomposScope.toHolder(
    flow: StateFlow<T>,
    context: CoroutineContext = EmptyCoroutineContext
): Holder<T> = toHolder(
    initial = flow.value,
    flow = flow,
    context = context,
)

fun <T> KomposScope.toHolder(
    initial: T,
    flow: Flow<T>,
    context: CoroutineContext = EmptyCoroutineContext
): Holder<T> {
    val result = keep { mutableHolderOf(initial) }
    onLaunch(flow, context) {
        if (context == EmptyCoroutineContext) {
            flow.collect { result.value = it }
        } else withContext(context) {
            flow.collect { result.value = it }
        }
    }
    return result
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
