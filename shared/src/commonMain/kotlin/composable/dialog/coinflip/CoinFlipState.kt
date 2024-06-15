package composable.dialog.coinflip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.heads
import lifelinked.shared.generated.resources.tails
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource

data class CoinFlipState(
    val headCount: Int = 0,
    val tailCount: Int = 0,
    val krarksThumbs: Int = 0,
    val history: List<CoinFace> = listOf(),
    val historyString: AnnotatedString = AnnotatedString(" "),
)

enum class CoinFace(val letter: String, val drawable: DrawableResource, val color: Color) {
    HEADS("H", Res.drawable.heads, Color.Green),
    TAILS("T", Res.drawable.tails, Color.Red),
    DIVIDER("|", Res.drawable.transparent, Color.White)
}