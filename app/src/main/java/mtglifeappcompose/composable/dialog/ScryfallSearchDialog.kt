package mtglifeappcompose.composable.dialog

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.launch
import mtglifeappcompose.data.Card
import mtglifeappcompose.data.Player
import mtglifeappcompose.data.ScryfallApiRetriever
import mtglifeappcompose.data.SharedPreferencesManager


@Composable
fun ScryfallSearchDialog(modifier: Modifier = Modifier, player: Player, onDismiss: () -> Unit) {
    SettingsDialog(modifier = modifier, backButtonEnabled = false, onDismiss = onDismiss)  {
        ScryfallSearchScreen(player = player)
    }
}

@Composable
fun ScryfallSearchScreen(modifier: Modifier = Modifier, player: Player) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Card>()) }
    val scryfallApiRetriever = ScryfallApiRetriever()
    val coroutineScope = rememberCoroutineScope()
    val lastSearchWasError = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    fun search(qry: String) {
        if (qry.isBlank()) return
        coroutineScope.launch {
            searchResults = listOf()
            searchResults = scryfallApiRetriever.parseScryfallResponse(scryfallApiRetriever.searchScryfall(qry))
            lastSearchWasError.value = searchResults.isEmpty()
        }
    }

    Column(modifier) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            TextField(value = query, onValueChange = { query = it }, label = { Text("Search Scryfall") }, singleLine = true, keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None, autoCorrect = false, keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
            ), keyboardActions = KeyboardActions(onSearch = {
                search(query)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                focusManager.clearFocus()
            }), colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                ),
                focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ), modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .border(
                    1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(10.dp)
                )
                .align(Alignment.CenterStart)
            )
            Box(
                Modifier
                    .wrapContentSize()
                    .padding(end = 10.dp)
                    .align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        search(query)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }, modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_icon), contentDescription = "Search", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp), visible = lastSearchWasError.value
        ) {
            Text("No cards found :(", color = Color.Red)
        }
        LazyColumn(
            Modifier.pointerInput(Unit) {
                detectTapGestures(onPress = { focusManager.clearFocus() })
            }, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (searchResults.isEmpty()) return@LazyColumn
            item {
                Text(
                    "${searchResults.size} results", color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            items(searchResults) { card ->
                CardPreview(card, onSelect = {
                    player.imageUri = Uri.parse(card.getUris().artCrop)
                    SharedPreferencesManager.savePlayer(player)
                }, onPrintings = {
                    search(card.printsSearchUri)
                })
            }
        }

    }
}


@Composable
fun ScryfallButton(modifier: Modifier = Modifier, text: String, onTap: () -> Unit) {
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { _ -> onTap() })
        },

        ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(10.dp)), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CardPreview(card: Card, onSelect: () -> Unit = {}, onPrintings: () -> Unit = {}) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showLargeImage) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        showLargeImage = false
                    })
                }) {
                Image(
                    modifier = Modifier
                        .clip(CutCornerShape(125.dp))
                        .fillMaxSize(0.85f)
                        .align(Alignment.Center), painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(card.getUris().large).crossfade(true).build()
                    ), contentDescription = null
                )
            }

        })
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(30.dp)
                )
                .clip(RoundedCornerShape(30.dp)), color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 15.dp)
                    .height(150.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2.0f), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        card.name, color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        card.setName, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Light
                    )
                    Text(
                        card.artist, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Light
                    )
                    Text(
                        "© Wizards of the Coast",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Row() {
                        ScryfallButton(modifier = Modifier
                            .width(80.dp)
                            .wrapContentHeight(), text = "Select", onTap = {
                            onSelect()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                        ScryfallButton(modifier = Modifier
                            .width(80.dp)
                            .wrapContentHeight(), text = "Printings", onTap = {
                            onPrintings()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                    }
                }
                Box(
                    Modifier
                        .fillMaxHeight()
                        .weight(1.0f)
                        .clip(CutCornerShape(6.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                showLargeImage = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        }) {
                    Image(
                        modifier = Modifier.fillMaxSize(), painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(card.getUris().small).crossfade(true).build()
                        ), contentDescription = null
                    )
                }
            }
        }
    }
}