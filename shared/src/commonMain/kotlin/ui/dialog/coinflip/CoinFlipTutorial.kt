package ui.dialog.coinflip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import theme.scaledSp
import ui.dialog.scryfall.ExpandableCard
import ui.flippable.Flippable
import ui.flippable.rememberFlipController


@Composable
fun CoinFlipTutorialContent(
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize(),
    ) {
        val padding = remember(Unit) { maxWidth / 50f + maxHeight / 75f }
        val textSize = remember(Unit) { (maxWidth / 50f + maxHeight / 150f).value }
        LazyColumn(
            modifier = modifier, verticalArrangement = Arrangement.spacedBy(padding / 6f), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    val flipController = rememberFlipController()
                    Flippable(
                        modifier = Modifier.fillMaxWidth(0.33f),
                        flipController = flipController,
                        frontSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/front/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                        backSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/back/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                    )
                    Text(text = """
                            Krark's Thumb is an artifact with the ability "If you would flip a coin, instead flip two coins and ignore one."
                            
                            This means that if you call heads, any number of heads will win the flip.
                            
                            This ability stacks exponentially, so if you have two thumbs, you flip four coins and ignore three, and so on.
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp, modifier = Modifier.fillMaxWidth().padding(padding / 2f).clickable {
                        flipController.flip()
                    })
                }
            }

            item {
                val flipController = rememberFlipController()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = buildAnnotatedString {
                        append("\"Flip until you lose\" means that you will keep flipping coins until you get don't get the result you called.\n\n")
                        append("With Krark's Thumb, this means you will keep flipping until you don't get ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("any")
                        }
                        append(" of the result you called.\n\n")
                        append("For example, the \"Call Heads\" button will flip until you fail to get a single head among all coins flipped.")
                    }, color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp, modifier = Modifier.fillMaxWidth(0.67f).padding(padding / 2f).clickable {
                        flipController.flip()
                    })
                    Flippable(
                        modifier = Modifier.fillMaxWidth(),
                        flipController = flipController,
                        frontSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/front/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                        backSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/back/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    val flipController = rememberFlipController()
                    Flippable(
                        modifier = Modifier.fillMaxWidth(0.33f),
                        flipController = flipController,
                        frontSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/front/7/f/7f2c332e-a5c9-40fb-a2fa-b4888aecc9c7.jpg?1694286113"
                            )
                        },
                        backSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://backs.scryfall.io/large/2/2/222b7a3b-2321-4d4c-af19-19338b134971.jpg?1677416389"
                            )
                        },
                    )
                    Text(text = """
                            You can adjust the number of Krark's Thumbs you have with the "Krark's Thumbs" buttons.
                        
                            You also may want to flip a specific amount of coins simultaneously, which you can set with the "Coins to Flip" button.
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp, modifier = Modifier.fillMaxWidth().padding(padding / 2f).clickable {
                        flipController.flip()
                    })
                }
            }

            item {
                Text(
                    text = """
                            Hint: if something breaks, the "Reset" button hopefully will fix it :)
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = textSize.scaledSp,
                    modifier = Modifier.padding(horizontal = padding * 1.5f).padding(bottom = padding)
                )
            }
        }
    }
}