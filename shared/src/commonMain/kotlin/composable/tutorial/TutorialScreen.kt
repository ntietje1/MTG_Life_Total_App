package composable.tutorial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import composable.SettingsButton
import composable.dialog.WarningDialog
import data.Player.Companion.allPlayerColors
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.skip_icon
import lifelinked.shared.generated.resources.tut_1
import lifelinked.shared.generated.resources.tut_2
import lifelinked.shared.generated.resources.tut_3
import lifelinked.shared.generated.resources.tut_4
import lifelinked.shared.generated.resources.tut_5
import lifelinked.shared.generated.resources.tut_6
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.absoluteValue

enum class SingleTutorialScreen(
    val imageResource: DrawableResource
) {
    TUTORIAL_1(Res.drawable.tut_1), TUTORIAL_2(Res.drawable.tut_2), TUTORIAL_3(Res.drawable.tut_3), TUTORIAL_4(Res.drawable.tut_4), TUTORIAL_5(Res.drawable.tut_5), TUTORIAL_6(Res.drawable.tut_6),
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
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

    Box(Modifier.fillMaxSize().background(Color.Black)) { // Background color has to be hard-coded since images have black background
        SettingsButton(modifier = Modifier.align(Alignment.BottomEnd).size(90.dp).padding(15.dp),
            mainColor = Color.White,
            backgroundColor = Color.Transparent,
            text = if (state.currentPage == state.totalPages - 1) "Close Tutorial" else "Skip Tutorial",
            shadowEnabled = false,
            imageVector = vectorResource(Res.drawable.skip_icon),
            onTap = {
                if (viewModel.settingsManager.tutorialSkip || state.currentPage == state.totalPages - 1) {
                    onFinishTutorial()
                } else {
                    viewModel.showWarningDialog()
                }
            })
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize().weight(0.9f).padding(15.dp),
                state = pagerState, key = { SingleTutorialScreen.entries[it] }, pageSize = PageSize.Fill,
                beyondBoundsPageCount = 4
            ) { index ->
                Image(
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillHeight, bitmap = imageResource(SingleTutorialScreen.entries[index].imageResource), contentDescription = null
                )
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

