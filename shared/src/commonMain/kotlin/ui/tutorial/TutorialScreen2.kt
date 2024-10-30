package ui.tutorial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.Player.Companion.allPlayerColors
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.placeholder_icon
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton
import ui.dialog.WarningDialog
import ui.lifecounter.LifeCounterScreen
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen2(
    viewModel: TutorialViewModel,
    onFinishTutorial: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(initialPage = state.currentPage, initialPageOffsetFraction = 0f, pageCount = { state.totalPages })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    if (state.showWarningDialog) {
        WarningDialog(
            title = "Warning",
            message = "Are you sure you want to skip the tutorial?",
            optionOneMessage = "Skip",
            onOptionOne = {
                onFinishTutorial()
            },
            optionTwoMessage = "Cancel",
            onOptionTwo = {},
            onDismiss = {
                viewModel.showWarningDialog(false)
            }
        )
    }

    var showInstructions by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().background(Color.Black)) { // Background color has to be hard-coded since images have black background
        SettingsButton(modifier = Modifier.align(Alignment.TopEnd).size(90.dp).padding(horizontal = 15.dp),
            mainColor = Color.White,
            backgroundColor = Color.Transparent,
            text = if (state.currentPage == state.totalPages - 1) "Close Tutorial" else "Skip Tutorial",
            shadowEnabled = false,
            imageVector = vectorResource(Res.drawable.x_icon),
            onTap = {
                if (viewModel.settingsManager.tutorialSkip || state.currentPage == state.totalPages - 1) {
                    onFinishTutorial()
                } else {
                    viewModel.showWarningDialog()
                }
            })
        SettingsButton(modifier = Modifier.align(Alignment.BottomStart).size(90.dp).padding(15.dp),
            mainColor = if (!showInstructions) Color.White else Color.White.copy(alpha=0.7f),
            backgroundColor = Color.Transparent,
            text = "Show Instructions",
            shadowEnabled = false,
            imageVector = vectorResource(Res.drawable.question_icon),
            onTap = {
                showInstructions = !showInstructions
            })
        Column(
            modifier = Modifier.fillMaxSize().padding(top=90.dp).align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize().weight(0.9f).padding(15.dp),
                state = pagerState, key = { SingleTutorialScreen.entries[it] }, pageSize = PageSize.Fill
            ) { index ->
                if (index == 0) {
                    //TODO: add a button to show instructions again
                    Box(Modifier.fillMaxSize()) {
                        LifeCounterScreen(
                            viewModel = koinInject(),
                            toggleTheme = {},
                            toggleKeepScreenOn = {},
                            goToPlayerSelectScreen = {},
                            goToTutorialScreen = {},
                            numPlayers = 4,
                            timerEnabled = false,
                            firstNavigation = false
                        )

                        if (showInstructions) {
                        Box(
                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).pointerInput(Unit) {
                                showInstructions = false
                            }
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SettingsButton(
                                    modifier = Modifier.size(90.dp),
                                    mainColor = Color.White,
                                    backgroundColor = Color.Transparent,
                                    shadowEnabled = false,
                                    imageVector = vectorResource(Res.drawable.placeholder_icon),
                                    enabled = false)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tap up/down on a player to adjust their life total",
                                    fontSize = 20.scaledSp,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    style = defaultTextStyle(),
                                )
                            }
                        }
                    }
                    }
                } else {
                    Image(
                        modifier = Modifier.fillMaxSize().clickable {
                            scope.launch {
                                if (pagerState.currentPage == state.totalPages - 1) {
                                    onFinishTutorial()
                                } else {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1
                                    )
                                }
                            }
                        },
                        contentScale = ContentScale.FillHeight,
                        bitmap = imageResource(SingleTutorialScreen.entries[index].imageResource),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.offset(y = -(16).dp).wrapContentWidth().height(50.dp).clip(RoundedCornerShape(100))
//                .background(MaterialTheme.colors.background)
                    .padding(8.dp)
                    .background(color = Color.White, shape = CircleShape),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Go back"
                    )
                }
                Box(
                    Modifier.fillMaxHeight().width(22.dp * SingleTutorialScreen.entries.size),
                    contentAlignment = Alignment.CenterStart
                ) {
                    SingleTutorialScreen.entries.forEachIndexed { index, _ ->
                        if (index != pagerState.currentPage) {
                            Box(
                                Modifier
                                    .offset(x = (index * 22).dp)
                                    .size(20.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                    )
                            )
                        }
                    }
                    Box(
                        Modifier
                            .jumpingDotTransition(pagerState, 0.25f)
                            .size(20.dp)
                            .background(
                                color = allPlayerColors[state.currentPage].copy(alpha = 0.7f),
                                shape = CircleShape,
                            )
                    )
                }

                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go forward"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.jumpingDotTransition(pagerState: PagerState, jumpScale: Float) =
    graphicsLayer {
        val pageOffset = pagerState.currentPageOffsetFraction
        val scrollPosition = pagerState.currentPage + pageOffset

        translationX = (scrollPosition * (size.width + 2.dp.roundToPx()))

        val scale: Float
        val targetScale = jumpScale - 1f

        scale = if (pageOffset.absoluteValue < .5) {
            1.0f + (pageOffset.absoluteValue * 2) * targetScale;
        } else {
            jumpScale + ((1 - (pageOffset.absoluteValue * 2)) * targetScale);
        }

        scaleX = scale
        scaleY = scale
    }

