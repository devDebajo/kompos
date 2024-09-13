package ru.debajo.kompos

import ru.debajo.kompos.spek.Spek
import java.security.MessageDigest
import java.util.UUID

fun KomposScope.layout(
    spek: Spek = Spek,
    name: String,
    content: KomposScope.() -> Unit,
    measurePolicy: KomposMeasurePolicy,
) {
    newNode(
        spek = spek,
        name = name,
        content = content,
        measurePolicy = measurePolicy,
    )
}

fun KomposScope.newNode(
    spek: Spek = Spek,
    name: String,
    content: KomposScope.() -> Unit = {},
    measurePolicy: KomposMeasurePolicy = DefaultKomposMeasurePolicy,
) {
    val nodeKey = getCurrentNodeUniqueKey()
    currentKomposer.startNode(name, nodeKey)
    currentKomposer.setMeasurePolicy(measurePolicy)
    currentKomposer.setSpek(spek)
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

    fun newKomposer(density: KomposDensity): Komposer {
        val newKomposer = Komposer(
            id = UUID.randomUUID().toString(),
            density = density,
        )
        komposers[newKomposer.id] = newKomposer
        return newKomposer
    }

    fun getOrCreateComposer(density: KomposDensity, id: String?): Komposer {
        return if (id == null) {
            newKomposer(density)
        } else {
            komposers[id] ?: newKomposer(density)
        }
    }
}

class Komposer(
    val id: String,
    val density: KomposDensity,
) {
    private val nodePool = KomposNodePool()
    private val operations = mutableListOf<TreeOperation>()

    fun startNode(name: String, key: String) {
        operations.add(TreeOperation.StartNode(name, key))
    }

    fun setMeasurePolicy(measurePolicy: KomposMeasurePolicy) {
        operations.add(TreeOperation.SetMeasurePolicy(measurePolicy))
    }

    fun setSpek(spek: Spek) {
        operations.add(TreeOperation.SetSpek(spek))
    }

    fun endNode() {
        operations.add(TreeOperation.EndNode)
    }

    fun startGroup() {
        operations.add(TreeOperation.StartGroup)
    }

    fun endGroup() {
        operations.add(TreeOperation.EndGroup)
    }

    fun buildTree(): KomposNode {
        val rootNode = nodePool.get(density, "root", "root_$id")
        val childNode = operations.readNode(0).second
        if (childNode != null) {
            rootNode.addChild(childNode)
        }
        return rootNode
    }

    private fun List<TreeOperation>.readNode(from: Int): Pair<Int, KomposNode?> {
        var index = from
        var readingGroup = false
        var node: KomposNode? = null
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
                        node = nodePool.get(density, operation.name, operation.key)
                    }
                }

                is TreeOperation.SetSpek -> node!!.apply(operation)
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

    private fun KomposNode.apply(operation: TreeOperation.SetSpek) {
        spek = operation.spek
    }

    private fun KomposNode.apply(operation: TreeOperation.SetMeasurePolicy) {
        childMeasurePolicy = operation.measurePolicy
    }

    private sealed interface TreeOperation {
        class StartNode(val name: String, val key: String) : TreeOperation
        class SetMeasurePolicy(val measurePolicy: KomposMeasurePolicy) : TreeOperation
        class SetSpek(val spek: Spek) : TreeOperation
        object EndNode : TreeOperation
        object StartGroup : TreeOperation
        object EndGroup : TreeOperation
    }
}