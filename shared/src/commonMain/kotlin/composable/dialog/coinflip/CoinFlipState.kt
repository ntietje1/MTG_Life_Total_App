package composable.dialog.coinflip

import androidx.compose.ui.graphics.Color
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.heads
import lifelinked.shared.generated.resources.tails
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource

data class CoinFlipState(
    val headCount: Int = 0,
    val tailCount: Int = 0,
    val baseCoins: Int = 1,
    val krarksThumbs: Int = 0,
    val flipInProgress: Boolean = false,
    val userInteractionEnabled: Boolean = true,
    val lastResults: List<CoinHistoryItem> = listOf(),
    val history: List<CoinHistoryItem> = listOf(),
)

enum class CoinHistoryItem(val letter: String, val drawable: DrawableResource, val color: Color) {
    HEADS("H", Res.drawable.heads, Color.Green),
    TAILS("T", Res.drawable.tails, Color.Red),
    HEADS_TARGET_MARKER("", Res.drawable.transparent, Color.White),
    TAILS_TARGET_MARKER("", Res.drawable.transparent, Color.White),
    MULTI_MODE_MARKER("", Res.drawable.transparent, Color.White),
    SINGLE_MODE_MARKER("", Res.drawable.transparent, Color.White),
    COMMA(",", Res.drawable.transparent, Color.White),
    L_DIVIDER_SINGLE("(", Res.drawable.transparent, Color.White),
    R_DIVIDER_SINGLE(")", Res.drawable.transparent, Color.White),
    L_DIVIDER_LIST("(", Res.drawable.transparent, Color.White),
    R_DIVIDER_LIST(")", Res.drawable.transparent, Color.White),
}