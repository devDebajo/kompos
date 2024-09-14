package ru.debajo.kompos.node

import ru.debajo.kompos.Komposer
import ru.debajo.kompos.utils.hash

@JvmInline
internal value class KomposCallKey private constructor(val key: String) {
    companion object {
        fun current(): KomposCallKey {
            return KomposCallKey(getCurrentCallStackKey())
        }

        fun root(komposer: Komposer): KomposCallKey {
            return KomposCallKey("root_${komposer.id}")
        }

        val Empty: KomposCallKey = KomposCallKey("")
    }
}

private fun getCurrentCallStackKey(): String {
    val trace = mutableListOf<String>()
    for (stackTraceElement in Thread.currentThread().stackTrace.drop(5)) {
        trace.add("${stackTraceElement.fileName}${stackTraceElement.className}${stackTraceElement.methodName}${stackTraceElement.lineNumber}".hash())
        if (stackTraceElement.methodName == "ensureNode") {
            break
        }
    }

    return trace.joinToString().hash()
}