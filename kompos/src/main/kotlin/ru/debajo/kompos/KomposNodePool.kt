package ru.debajo.kompos

class KomposNodePool {

    private val free: HashSet<KomposNodePooled> = HashSet()
    private val buzy: HashSet<KomposNodePooled> = HashSet()

    fun get(density: KomposDensity, name: String): KomposNodePooled {
        if (free.isEmpty()) {
            return KomposNodePooled().also {
                buzy.add(it)
                it.density = density
                it.name = name
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
    }
}
