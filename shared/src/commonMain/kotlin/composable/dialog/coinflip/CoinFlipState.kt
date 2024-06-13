package composable.dialog.coinflip

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import composable.flippable.FlipAnimationType
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.heads
import lifelinked.shared.generated.resources.tails
import org.jetbrains.compose.resources.DrawableResource

data class CoinFlipState(
    val currentFace: CoinFace = CoinFace.HEADS,
    val history: SnapshotStateList<CoinFace> = mutableStateListOf(),
    val flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE,
    val totalFlips: Int = 2, // + 1
    val flipCount: Int = totalFlips,
)

enum class CoinFace(val letter: String, val altLetter: String, val drawable: DrawableResource, val color: Color) {
    HEADS("H", "W", Res.drawable.heads, Color.Green),
    TAILS("T", "L", Res.drawable.tails, Color.Red),
}