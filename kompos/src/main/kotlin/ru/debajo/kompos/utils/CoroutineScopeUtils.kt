package ru.debajo.kompos.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

internal fun CoroutineScope.child(): CoroutineScope {
    return CoroutineScope(SupervisorJob() + coroutineContext)
}
