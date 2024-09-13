package ru.debajo.kompos

import ru.debajo.kompos.node.KomposMeasurePolicy
import ru.debajo.kompos.node.KomposNode
import ru.debajo.kompos.node.KomposNodeKey
import ru.debajo.kompos.node.KomposNodePool
import ru.debajo.kompos.spek.Spek
import java.util.UUID

class Komposer(
    internal val id: String,
    internal val density: KomposDensity,
) {
    private val nodePool = KomposNodePool()
    private val operations = mutableListOf<TreeOperation>()

    internal fun startNode(name: String, key: KomposNodeKey) {
        operations.add(TreeOperation.StartNode(name, key))
    }

    internal fun setMeasurePolicy(measurePolicy: KomposMeasurePolicy) {
        operations.add(TreeOperation.SetMeasurePolicy(measurePolicy))
    }

    internal fun setSpek(spek: Spek) {
        operations.add(TreeOperation.SetSpek(spek))
    }

    internal fun endNode() {
        operations.add(TreeOperation.EndNode)
    }

    internal fun startGroup() {
        operations.add(TreeOperation.StartGroup)
    }

    internal fun endGroup() {
        operations.add(TreeOperation.EndGroup)
    }

    internal fun buildTree(): KomposNode {
        val rootNode = nodePool.get(density, "root", KomposNodeKey.root(this))
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
        class StartNode(val name: String, val key: KomposNodeKey) : TreeOperation
        class SetMeasurePolicy(val measurePolicy: KomposMeasurePolicy) : TreeOperation
        class SetSpek(val spek: Spek) : TreeOperation
        object EndNode : TreeOperation
        object StartGroup : TreeOperation
        object EndGroup : TreeOperation
    }
}

internal object GlobalKomposer {
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
