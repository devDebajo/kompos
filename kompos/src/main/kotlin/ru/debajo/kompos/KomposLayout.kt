package ru.debajo.kompos

import ru.debajo.kompos.komposifier.Komposifier
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
    val scope = KomposScopeImpl(name, this, komposifier)
    scope.content()
    scope.registerMeasurePolicy(measurePolicy)
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

}