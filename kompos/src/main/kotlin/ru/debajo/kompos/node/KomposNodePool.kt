package ru.debajo.kompos.node

import ru.debajo.kompos.KomposDensity

internal class KomposNodePool {

    private val free: HashSet<KomposNode> = HashSet()
    private val buzy: HashSet<KomposNode> = HashSet()

    fun get(density: KomposDensity, name: String, key: KomposCallKey): KomposNode {
        if (free.isEmpty()) {
            return KomposNode().also {
                it.inflate(density, name, key)
                buzy.add(it)
            }
        }
        val node = free.first()
        node.inflate(density, name, key)
        free.remove(node)
        buzy.add(node)
        return node
    }

    fun recycleAll() {
        buzy.forEach {
            it.clear()
            free.add(it)
        }
        buzy.clear()
    }
}
