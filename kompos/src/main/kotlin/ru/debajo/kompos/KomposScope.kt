package ru.debajo.kompos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.debajo.kompos.holder.Holder
import ru.debajo.kompos.holder.toHolder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface KomposScope : KomposDensity {
    val currentKomposer: Komposer

    fun <T> StateFlow<T>.toHolder(
        context: CoroutineContext = EmptyCoroutineContext
    ): Holder<T> = toHolder(flow = this, context = context)

    fun <T> Flow<T>.toHolder(
        initial: T,
        context: CoroutineContext = EmptyCoroutineContext
    ): Holder<T> = toHolder(initial = initial, flow = this, context = context)
}
