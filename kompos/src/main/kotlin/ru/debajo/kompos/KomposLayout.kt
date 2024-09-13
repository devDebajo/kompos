package ru.debajo.kompos

import android.util.Log
import ru.debajo.kompos.komposifier.Komposifier
import java.security.MessageDigest
import java.util.UUID

fun KomposScope.layout(
    komposifier: Komposifier = Komposifier,
    name: String,
    content: KomposScope.() -> Unit,
    measurePolicy: KomposMeasurePolicy,
) {
    newNode(
        komposifier = komposifier,
        name = name,
        content = content,
        measurePolicy = measurePolicy,
    )
}

fun KomposScope.newNode(
    komposifier: Komposifier = Komposifier,
    name: String,
    content: KomposScope.() -> Unit = {},
    measurePolicy: KomposMeasurePolicy = DefaultKomposMeasurePolicy,
) {
    val nodeKey = getCurrentNodeUniqueKey()
    currentKomposer.startNode(name, nodeKey)
    currentKomposer.setMeasurePolicy(measurePolicy)
    currentKomposer.setKomposifier(komposifier)
    currentKomposer.startGroup()
    content()
    currentKomposer.endGroup()
    currentKomposer.endNode()
}

private fun getCurrentNodeUniqueKey(): String {
    val trace = mutableListOf<String>()
    for (stackTraceElement in Thread.currentThread().stackTrace.drop(3)) {
        trace.add("${stackTraceElement.fileName}${stackTraceElement.className}${stackTraceElement.methodName}${stackTraceElement.lineNumber}".hash())
        if (stackTraceElement.methodName == "setContent") {
            break
        }
    }

    return trace.joinToString().hash()
}

private val digest = MessageDigest.getInstance("SHA-256")

private fun String.hash(): String {
    return bytesToHex(digest.digest(toByteArray()))
}

private fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuilder(2 * hash.size)
    for (i in hash.indices) {
        val hex = Integer.toHexString(0xff and hash[i].toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}

object GlobalKomposer {
    private val komposers: MutableMap<String, Komposer> = HashMap()

    fun newKomposer(): Komposer {
        val newKomposer = Komposer(UUID.randomUUID().toString())
        komposers[newKomposer.id] = newKomposer
        return newKomposer
    }

    fun getOrCreateComposer(id: String?): Komposer {
        return if (id == null) {
            newKomposer()
        } else {
            komposers[id] ?: newKomposer()
        }
    }
}

class Komposer(val id: String) {

    private val nodePool = KomposNodePool()
    private val operations = mutableListOf<TreeOperation>()

    fun startNode(name: String, key: String) {
        Log.d("yopta", "startNode $name $key")
        operations.add(TreeOperation.StartNode(name, key))
    }

    fun setMeasurePolicy(measurePolicy: KomposMeasurePolicy) {
        Log.d("yopta", "setMeasurePolicy")
        operations.add(TreeOperation.SetMeasurePolicy(measurePolicy))
    }

    fun setKomposifier(komposifier: Komposifier) {
        Log.d("yopta", "setKomposifier")
        operations.add(TreeOperation.SetKomposifier(komposifier))
    }

    fun endNode() {
        Log.d("yopta", "endNode")
        operations.add(TreeOperation.EndNode)
    }

    fun startGroup() {
        Log.d("yopta", "startGroup")
        operations.add(TreeOperation.StartGroup)
    }

    fun endGroup() {
        Log.d("yopta", "endGroup")
        operations.add(TreeOperation.EndGroup)
    }

    fun buildTree(): KomposNodePooled {
        val rootNode = nodePool.get()
        rootNode.name = "root"
        val childNode = operations.readNode(0).second
        if (childNode != null) {
            rootNode.addChild(childNode)
        }
        return rootNode
    }

    private fun List<TreeOperation>.readNode(from: Int): Pair<Int, KomposNodePooled?> {
        var index = from
        var readingGroup = false
        var node: KomposNodePooled? = null
        while (index <= lastIndex) {
            when (val operation = this[index]) {
                is TreeOperation.StartNode -> {
                    if (readingGroup) {
                        val (lastIndex, childNode) = readNode(from = index)
                        if (childNode != null) {
                            node!!.addChild(childNode)
                        }
                        index = lastIndex
                    } else {
                        node = nodePool.get()
                        node.name = operation.name
                    }
                }

                is TreeOperation.SetKomposifier -> node!!.apply(operation)
                is TreeOperation.SetMeasurePolicy -> node!!.apply(operation)
                is TreeOperation.StartGroup -> {
                    readingGroup = true
                    val (lastIndex, childNode) = readNode(from = index + 1)
                    if (childNode != null) {
                        node!!.addChild(childNode)
                    }
                    index = lastIndex
                }

                is TreeOperation.EndGroup -> {
                    if (!readingGroup && node == null) {
                        return index to null
                    }
                    readingGroup = false
                }

                is TreeOperation.EndNode -> return index to node
            }
            index++
        }
        error("Node not ended")
    }

    private fun KomposNodePooled.apply(operation: TreeOperation.SetKomposifier) {
        komposifier = operation.komposifier
    }

    private fun KomposNodePooled.apply(operation: TreeOperation.SetMeasurePolicy) {
        childMeasurePolicy = operation.measurePolicy
    }

    private sealed interface TreeOperation {
        class StartNode(val name: String, val key: String) : TreeOperation
        class SetMeasurePolicy(val measurePolicy: KomposMeasurePolicy) : TreeOperation
        class SetKomposifier(val komposifier: Komposifier) : TreeOperation
        object EndNode : TreeOperation
        object StartGroup : TreeOperation
        object EndGroup : TreeOperation
    }
}