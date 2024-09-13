package ru.debajo.kompos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.height
import ru.debajo.kompos.spek.padding
import ru.debajo.kompos.widget.KomposAlignment
import ru.debajo.kompos.widget.box
import ru.debajo.kompos.widget.column
import ru.debajo.kompos.widget.row
import ru.debajo.kompos.widget.text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidView(factory = {
                KomposView(it).apply {
                    describeUi { test() }
                }
            })
        }
    }

    private fun KomposScope.test() {
//        box(
//            contentVerticalAlignment = KomposAlignment.Center,
//            contentHorizontalAlignment = KomposAlignment.Center,
//            spek = Spek
//                .size(47.kdp)
//                .background(Kolor.Black)
//        ) {
//            drawable(R.drawable.ic_launcher_foreground)
//            text("test")
//        }

        box {
            column {
                row {
                    text("text1", textSize = 30.ksp)
                    text(
                        "text2",
                        spek = Spek
                            .clip(KomposRoundedCornerShape(10.kdp))
                            .background(Kolor.Red)
                            .padding(8.kdp)
                            .background(Kolor.Blue),
                        color = Kolor.White
                    )
                    text("text3")
                }
                text("text4")
                text("text5")
                text("text6")
            }
        }
    }

    private fun KomposScope.button(
        spek: Spek = Spek,
        text: String,
        onClick: () -> Unit
    ) {
        box(
            contentVerticalAlignment = KomposAlignment.Center,
            contentHorizontalAlignment = KomposAlignment.Start,
            spek = spek
                .height(40.kdp)
                .clip(KomposRoundedCornerShape(8.kdp))
                .background(Kolor.Gray)
                .padding(horizontal = 16.kdp)
                .clickable(onClick = onClick)
        ) {
            text(text = text)
        }
    }
}
