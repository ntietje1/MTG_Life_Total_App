package ui.dialog.coinflip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.min
import theme.LocalDimensions
import theme.scaledSp
import ui.components.EnlargeableCardImage
import ui.flippable.Flippable
import ui.flippable.rememberFlipController


@Composable
fun CoinFlipTutorialContent(
    modifier: Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize(),
    ) {
        val cardWidth = min(maxWidth / 3, (maxHeight / 4) / (7/5f))
        val textWidth = maxWidth - cardWidth - dimensions.paddingMedium * 2
        LazyColumn(
            modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                val flipController = rememberFlipController()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(dimensions.paddingMedium), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Flippable(
                        modifier = Modifier.width(cardWidth),
                        flipController = flipController,
                        frontSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://cards.scryfall.io/normal/front/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752",
                                largeImageUri = "https://cards.scryfall.io/large/front/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                        backSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://cards.scryfall.io/normal/back/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752",
                                largeImageUri = "https://cards.scryfall.io/large/back/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                    )
                    Text(text = """
                            Krark's Thumb is an artifact with the ability "If you would flip a coin, instead flip two coins and ignore one."
                            
                            This means that if you call heads, any number of heads will win the flip.
                            
                            This ability stacks exponentially, so if you have two thumbs, you flip four coins and ignore three, and so on.
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary, fontSize = dimensions.textSmall.scaledSp, modifier = Modifier.fillMaxWidth().padding(dimensions.paddingMedium).clickable {
                        flipController.flip()
                    })
                }
            }

            item {
                val flipController = rememberFlipController()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(dimensions.paddingMedium), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = buildAnnotatedString {
                        append("\"Flip until you lose\" means that you will keep flipping coins until you get don't get the result you called.\n\n")
                        append("With Krark's Thumb, this means you will keep flipping until you don't get ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("any")
                        }
                        append(" of the result you called.\n\n")
                        append("For example, the \"Call Heads\" button will flip until you fail to get a single head among all coins flipped.")
                    }, color = MaterialTheme.colorScheme.onPrimary, fontSize = dimensions.textSmall.scaledSp, modifier = Modifier.width(textWidth).padding(dimensions.paddingMedium).clickable {
                        flipController.flip()
                    })
                    Flippable(
                        modifier = Modifier.fillMaxWidth(),
                        flipController = flipController,
                        frontSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://cards.scryfall.io/normal/front/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689",
                                largeImageUri = "https://cards.scryfall.io/large/front/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                        backSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://cards.scryfall.io/normal/back/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689",
                                largeImageUri = "https://cards.scryfall.io/large/back/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                    )
                }
            }

            item {
                val flipController = rememberFlipController()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(dimensions.paddingMedium), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Flippable(
                        modifier = Modifier.width(cardWidth),
                        flipController = flipController,
                        frontSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://cards.scryfall.io/normal/front/7/f/7f2c332e-a5c9-40fb-a2fa-b4888aecc9c7.jpg?1694286113",
                                largeImageUri = "https://cards.scryfall.io/large/front/7/f/7f2c332e-a5c9-40fb-a2fa-b4888aecc9c7.jpg?1694286113"
                            )
                        },
                        backSide = {
                            EnlargeableCardImage(
                                modifier = Modifier.fillMaxSize(),
                                smallImageUri = "https://backs.scryfall.io/normal/2/2/222b7a3b-2321-4d4c-af19-19338b134971.jpg?1677416389",
                                largeImageUri = "https://backs.scryfall.io/large/2/2/222b7a3b-2321-4d4c-af19-19338b134971.jpg?1677416389",
                            )
                        },
                    )
                    Text(text = """
                            You can adjust the number of Krark's Thumbs you have with the "Krark's Thumbs" buttons.
                        
                            You also may want to flip a specific amount of coins simultaneously, which you can set with the "Coins to Flip" button.
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary, fontSize = dimensions.textSmall.scaledSp, modifier = Modifier.fillMaxWidth().padding(dimensions.paddingMedium).clickable {
                        flipController.flip()
                    })
                }
            }

            item {
                Text(
                    text = """
                            Hint: if something breaks, the "Reset" button hopefully will fix it :)
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = dimensions.textSmall.scaledSp,
                    modifier = Modifier.padding(horizontal = dimensions.paddingLarge).padding(bottom = dimensions.paddingLarge)
                )
            }
        }
    }
}