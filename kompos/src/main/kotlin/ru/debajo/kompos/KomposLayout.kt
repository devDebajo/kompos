package ru.debajo.kompos

import ru.debajo.kompos.node.DefaultKomposMeasurePolicy
import ru.debajo.kompos.node.KomposMeasurePolicy
import ru.debajo.kompos.node.KomposNodeKey
import ru.debajo.kompos.spek.Spek

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
    val nodeKey = KomposNodeKey.current()
    currentKomposer.startNode(name, nodeKey)
    currentKomposer.setMeasurePolicy(measurePolicy)
    currentKomposer.setSpek(spek)
    currentKomposer.startGroup()
    content()
    currentKomposer.endGroup()
    currentKomposer.endNode()
}
