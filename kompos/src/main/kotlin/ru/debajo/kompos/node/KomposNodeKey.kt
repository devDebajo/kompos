package ru.debajo.kompos.node

import ru.debajo.kompos.Komposer
import ru.debajo.kompos.utils.hash

@JvmInline
internal value class KomposNodeKey private constructor(val key: String) {
    companion object {
        fun current(): KomposNodeKey {
            return KomposNodeKey(getCurrentCallStackKey())
        }

        fun root(komposer: Komposer): KomposNodeKey {
            return KomposNodeKey("root_${komposer.id}")
        }

        val Empty: KomposNodeKey = KomposNodeKey("")
    }
}

private fun getCurrentCallStackKey(): String {
    val trace = mutableListOf<String>()
    for (stackTraceElement in Thread.currentThread().stackTrace.drop(3)) {
        trace.add("${stackTraceElement.fileName}${stackTraceElement.className}${stackTraceElement.methodName}${stackTraceElement.lineNumber}".hash())
        if (stackTraceElement.methodName == "describeUi") {
            break
        }
    }

    return trace.joinToString().hash()
}