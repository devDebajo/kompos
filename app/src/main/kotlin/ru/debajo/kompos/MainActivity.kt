package ru.debajo.kompos

import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import ru.debajo.kompos.holder.getValue
import ru.debajo.kompos.spek.Spek
import ru.debajo.kompos.spek.height
import ru.debajo.kompos.spek.padding
import ru.debajo.kompos.utils.onLaunch
import ru.debajo.kompos.widget.KomposAlignment
import ru.debajo.kompos.widget.box
import ru.debajo.kompos.widget.column
import ru.debajo.kompos.widget.row
import ru.debajo.kompos.widget.text
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val komposView1 = findViewById<KomposView>(R.id.kompos1)
        val komposView2 = findViewById<KomposView>(R.id.kompos2)

        val counterHolder = MutableStateFlow(0)

        komposView1.describeUi {
            val counter by counterHolder.toHolder()
            test(text = "Counter: $counter")
        }

        komposView2.describeUi {
            onLaunch(Unit) {
                while (true) {
                    delay(1000)
                    counterHolder.value = Random.nextInt()
                }
            }

            val counter by counterHolder.toHolder()
            test(text = "Counter: $counter")
        }
    }

    private fun KomposScope.test(text: String) {
        box {
            column {
                row {
                    text("text1", textSize = 30.ksp)
                    text(
                        text = text,
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
