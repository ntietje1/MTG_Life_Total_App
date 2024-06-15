package composable.dialog.coinflip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import composable.flippable.FlipAnimationType
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.heads
import lifelinked.shared.generated.resources.tails
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource

data class CoinFlipState(
    val currentFace: CoinFace = CoinFace.HEADS,
    val headCount: Int = 0,
    val tailCount: Int = 0,
    val history: List<CoinFace> = listOf(),
    val historyString: AnnotatedString = AnnotatedString(""),
    val flipInProgress: Boolean = false,
    val flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE,
    val flippingUntil: CoinFace? = null,
    val flipCount: Int = Int.MAX_VALUE,
    val duration: Int = Int.MAX_VALUE,
)

enum class CoinFace(val letter: String, val drawable: DrawableResource, val color: Color) {
    HEADS("H", Res.drawable.heads, Color.Green),
    TAILS("T", Res.drawable.tails, Color.Red),
    DIVIDER("|", Res.drawable.transparent, Color.White)
}