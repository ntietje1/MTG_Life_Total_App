import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import theme.LocalDimensions
import theme.scaledSp
import ui.components.SelectableEnlargeableCardImage

@Composable
fun PlanechaseTutorialContent(
    modifier: Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize(),
    ) {
        val textSize = remember(Unit) { (maxWidth / 50f + maxHeight / 150f).value }
        LazyColumn(
            modifier = modifier.padding(dimensions.paddingMedium),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = dimensions.paddingSmall)
                        .padding(top = dimensions.paddingSmall),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = """
                            Planechase is a casual Magic: The Gathering variant where players travel between different planes, each with unique effects that modify the game.
                        """.trimIndent(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = textSize.scaledSp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.paddingSmall)
                    )
                }
            }
            item {
                SelectableEnlargeableCardImage(
                    modifier = Modifier
                        .height(dimensions.screenWidth / 1.5f)
                        .padding(horizontal = dimensions.screenWidth / 20f)
                        .rotate(90f),
                    normalImageUri = "https://cards.scryfall.io/large/front/d/8/d8da872d-55e0-4596-ba8e-f9ff7b2c0a86.jpg?1680815480",
                    largeImageUri = "https://cards.scryfall.io/large/front/d/8/d8da872d-55e0-4596-ba8e-f9ff7b2c0a86.jpg?1680815480",
                    showSelectedBackground = false
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = dimensions.paddingSmall),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = """
                            To begin, select your planar deck using the "Planar Deck" button. You can search for specific planes and toggle their selection.
                            
                            Deck Building Rules:
                            • A planar deck must contain at least 40 cards, or at least 10x the number of players, whichever is smaller
                            • The deck cannot contain more phenomenon cards than 2x the number of players
                            
                            It is recommended to use a separate device to display the Planechase screen side by side with your game, making it easily visible to all players.
                        """.trimIndent(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = textSize.scaledSp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.paddingSmall)
                    )
                }
            }
            item {
                SelectableEnlargeableCardImage(
                    modifier = Modifier
                        .height(dimensions.screenWidth / 1.5f)
                        .padding(horizontal = dimensions.screenWidth / 20f)
                        .rotate(90f),
                    normalImageUri = "https://cards.scryfall.io/large/front/b/e/be4da23c-bc51-4601-8f86-4e6f4eb27e6a.jpg?1680815547",
                    largeImageUri = "https://cards.scryfall.io/large/front/b/e/be4da23c-bc51-4601-8f86-4e6f4eb27e6a.jpg?1680815547",
                    showSelectedBackground = false
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = dimensions.paddingSmall),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = """
                            During the game:
                            • Long press a plane card to view it in full size
                            • Use "Previous" to return to the last plane
                            • "Flip Image" rotates the current plane card
                            • "Planeswalk" moves to the next plane
                            • Roll the "Planar Die" which can trigger:
                              - Planeswalk (1/6): Move to next plane
                              - Chaos (1/6): Trigger plane's chaos ability
                              - Blank (4/6): No effect (most common)
                            
                            On your turn, you may roll the planar die by paying mana. The first roll is free, and each additional roll costs one more mana (e.g., second roll costs {1}, third roll costs {2}, etc.). This cost resets at the beginning of your next turn.
                        """.trimIndent(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = textSize.scaledSp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.paddingSmall)
                            .padding(bottom = dimensions.paddingSmall)
                    )
                }
            }
        }
    }
}