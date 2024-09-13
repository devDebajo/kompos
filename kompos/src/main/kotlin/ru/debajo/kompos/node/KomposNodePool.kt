package ru.debajo.kompos.node

import ru.debajo.kompos.KomposDensity

internal class KomposNodePool {

    private val free: HashSet<KomposNode> = HashSet()
    private val buzy: HashSet<KomposNode> = HashSet()

    fun get(density: KomposDensity, name: String, key: KomposNodeKey): KomposNode {
        if (free.isEmpty()) {
            return KomposNode().also {
                it.inflate(density, name, key)
                buzy.add(it)
            }
        }
        val node = free.first()
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
