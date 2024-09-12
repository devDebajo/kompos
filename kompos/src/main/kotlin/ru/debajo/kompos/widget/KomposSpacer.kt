package ru.debajo.kompos.widget

import ru.debajo.kompos.KomposScope
import ru.debajo.kompos.komposifier.Komposifier

fun KomposScope.spacer(
    komposifier: Komposifier = Komposifier,
) {
    box(komposifier) { }
}
