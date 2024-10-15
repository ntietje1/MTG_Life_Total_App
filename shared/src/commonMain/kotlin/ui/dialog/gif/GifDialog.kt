package ui.dialog.gif

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ui.dialog.scryfall.ScryfallSearchBar
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonBackground

@Composable
fun GifDialogContent(
    modifier: Modifier = Modifier,
    onGifSelected: (String) -> Unit,
    viewModel: GifDialogViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyGridState(state.scrollPosition)
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.setScrollPosition(listState.firstVisibleItemIndex)
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val searchBarHeight = remember(Unit) { maxWidth / 9f + 30.dp }
        val padding = remember(Unit) { searchBarHeight / 10f }
        val buttonWidth = remember { (maxWidth / 2f) - 16.dp }
        val buttonHeight = remember { buttonWidth / 1.75f }
        val numberToQuery = remember { (maxHeight / buttonHeight).toInt() * 2 }

        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            val endReached = listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset >= state.gifResults.size - numberToQuery
            if (endReached) {
                viewModel.getNextGifs(numberToQuery)
            }
        }


        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScryfallSearchBar(
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(searchBarHeight)
                    .padding(top = padding)
                    .clip(RoundedCornerShape(15))
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(15)),
                query = state.textFieldValue,
                label = "Search Tenor",
                onQueryChange = viewModel::setTextFieldValue,
                searchInProgress = state.isSearchInProgress
            ) {
                focusManager.clearFocus()
                viewModel.searchGifs(state.textFieldValue.text, numberToQuery)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                Modifier.pointerInput(Unit) {
                    detectTapGestures(onPress = { focusManager.clearFocus() })
                }, horizontalArrangement = Arrangement.Center,
                state = listState
            ) {
                items(
                    items = state.gifResults.toList(),
                    key = { it.hashCode() }
                ) { mediaFormats ->
                    PlayerButtonBackground(
                        modifier = Modifier
                            .padding(8.dp)
                            .width(buttonWidth)
                            .height(buttonHeight)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        onGifSelected(mediaFormats.getNormalGif().url)
                                    }
                                )
                            },
                        state = PBState.NORMAL,
                        isDead = false,
                        imageUri = mediaFormats.getPreviewGif().url,
                        color = Color.Black,
                    )
                }
            }
        }
    }
}