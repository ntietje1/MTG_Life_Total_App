package ui.dialog.coinflip

import androidx.compose.material3.MaterialTheme
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
    val baseCoins: Int = 1,
    val krarksThumbs: Int = 0,
    val flipInProgress: Boolean = false,
    val userInteractionEnabled: Boolean = true,
    val lastResultString: AnnotatedString = AnnotatedString(""),
    val lastResults: List<CoinHistoryItem> = listOf(),
    val historyString: AnnotatedString = AnnotatedString(""),
    val history: List<CoinHistoryItem> = listOf(),
)

enum class CoinHistoryItem(val letter: String = "", val drawable: DrawableResource = Res.drawable.transparent, val color: Color = Color.Unspecified) {
    HEADS("H", Res.drawable.heads, Color.Green),
    TAILS("T", Res.drawable.tails, Color.Red),
    HEADS_TARGET_MARKER,
    TAILS_TARGET_MARKER,
    MULTI_MODE_MARKER,
    SINGLE_MODE_MARKER,
    COMMA(","),
    L_DIVIDER_SINGLE("("),
    R_DIVIDER_SINGLE(")"),
    L_DIVIDER_LIST("("),
    R_DIVIDER_LIST(")"),
}