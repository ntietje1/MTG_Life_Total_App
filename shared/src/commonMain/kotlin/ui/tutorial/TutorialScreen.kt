package ui.tutorial

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import di.NotificationManager
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import ui.SettingsButton
import ui.dialog.WarningDialog
import ui.tutorial.pages.TutorialPage1
import ui.tutorial.pages.TutorialPage2
import ui.tutorial.pages.TutorialPage3
import ui.tutorial.pages.TutorialPage4
import ui.tutorial.pages.TutorialPage5
import kotlin.math.absoluteValue

data class TutorialStep(
    val content: @Composable () -> Unit
)

@Composable
fun TutorialScreen(
    viewModel: TutorialViewModel,
    notificationManager: NotificationManager = koinInject(),
    onFinishTutorial: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(initialPage = state.currentPage, initialPageOffsetFraction = 0f, pageCount = { state.totalPages })
    val scope = rememberCoroutineScope()
    val animationSpec = remember { tween<Float>(durationMillis = 750, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)) }
    val fastAnimationSpec = remember { tween<Float>(durationMillis = 400, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)) }

    suspend fun animateToNextPage(increment: Int) {
        viewModel.onChangePage()
        pagerState.animateScrollToPage(
            page = pagerState.currentPage + increment,
            animationSpec = if (pagerState.currentPageOffsetFraction.absoluteValue > 0f) fastAnimationSpec else  animationSpec
        )
    }

    fun showSingleHint(pageIndex: Int): Boolean {
        return if (pagerState.currentPageOffsetFraction < 0.25f && pagerState.currentPage == pageIndex) {
            true
        } else if (pagerState.currentPageOffsetFraction > 0.25f && pagerState.currentPage == pageIndex + 1) {
            true
        } else if (pagerState.currentPageOffsetFraction < -0.25f && pagerState.currentPage == pageIndex - 1) {
            true
        } else {
            false
        }
    }

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

    if (state.showCloseDialog) {
        WarningDialog(
            title = "Warning",
            message = "Are you sure you want to close the tutorial?",
            optionOneMessage = "Close",
            onOptionOne = {
                onFinishTutorial()
            },
            optionTwoMessage = "Cancel",
            onOptionTwo = {},
            onDismiss = {
                viewModel.showCloseDialog(false)
            }
        )
    }

    val tutorialSteps = listOf(
        TutorialStep(
            content = {
                TutorialPage1(
                    modifier = Modifier.fillMaxSize(),
                    showHint = state.showHint && showSingleHint(0),
                    onHintDismiss = { viewModel.showHint(false) },
                    onComplete = {
                        if (!state.completed[state.currentPage]) {
                            viewModel.showHint(false)
                            viewModel.setSuccess(true)
                            notificationManager.showNotification("Success! Tap below to move onto the next screen", 3000)
                        }
                    }
                )
            }
        ),
        TutorialStep(
            content = {
                TutorialPage2(
                    modifier = Modifier.fillMaxSize(),
                    showHint = state.showHint && showSingleHint(1),
                    onHintDismiss = { viewModel.showHint(false) },
                    onComplete = {
                        if (!state.completed[state.currentPage]) {
                            viewModel.showHint(false)
                            viewModel.setSuccess(true)
                            notificationManager.showNotification("Success! Tap below to move onto the next screen", 3000)
                        }
                    }
                )
            }
        ),
        TutorialStep(
            content = {
                TutorialPage3(
                    modifier = Modifier.fillMaxSize(),
                    showHint = state.showHint && showSingleHint(2),
                    onHintDismiss = { viewModel.showHint(false) },
                    onComplete = {
                        if (!state.completed[state.currentPage]) {
                            viewModel.showHint(false)
                            viewModel.setSuccess(true)
                            notificationManager.showNotification("Success! Tap below to move onto the next screen", 3000)
                        }
                    }
                )
            }
        ),
        TutorialStep(
            content = {
                TutorialPage4(
                    modifier = Modifier.fillMaxSize(),
                    showHint = state.showHint && showSingleHint(3),
                    onHintDismiss = { viewModel.showHint(false) },
                    setBlurUI = { viewModel.setBlur(it) },
                    onComplete = {
                        if (!state.completed[state.currentPage]) {
                            viewModel.showHint(false)
                            viewModel.setSuccess(true)
                            notificationManager.showNotification("Success! Tap below to move onto the next screen", 3000)
                        }
                    }
                )
            }
        ),
        TutorialStep(
            content = {
                TutorialPage5(
                    modifier = Modifier.fillMaxSize(),
                    showHint = state.showHint && showSingleHint(4),
                    onHintDismiss = { viewModel.showHint(false) },
                    setBlurUI = { viewModel.setBlur(it) },
                    onComplete = {
                        if (!state.completed[state.currentPage]) {
                            viewModel.showHint(false)
                            viewModel.setSuccess(true)
                            notificationManager.showNotification("Success! Tap below to move onto the next screen", 3000)
                        }
                    }
                )
            }
        )
    )

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) { // Background color has to be hard-coded since images have black background
        val blurRadius = remember(Unit) { maxHeight / 75f }
        Box(
            Modifier.wrapContentSize().then(if (state.blur) Modifier.blur(blurRadius) else Modifier)
        ) {
            SettingsButton(modifier = Modifier.align(Alignment.TopEnd).size(100.dp).padding(horizontal = 15.dp),
                mainColor = Color.White,
                backgroundColor = Color.Transparent,
                text = if (state.currentPage == state.totalPages - 1) "Close Tutorial" else "Skip Tutorial",
                shadowEnabled = false,
                imageVector = vectorResource(Res.drawable.x_icon),
                onTap = {
                    if (state.currentPage == state.totalPages - 1) {
                        viewModel.showCloseDialog(true)
                    } else {
                        viewModel.showWarningDialog(true)
                    }
                })
            SettingsButton(modifier = Modifier.align(Alignment.BottomStart).size(90.dp).padding(15.dp),
                mainColor = if (!state.showHint) Color.White else Color.White.copy(alpha = 0.7f),
                backgroundColor = Color.Transparent,
                text = "Show Hint",
                shadowEnabled = false,
                imageVector = vectorResource(Res.drawable.question_icon),
                onTap = {
                    viewModel.showHint(!state.showHint)
                })
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 80.dp).align(Alignment.TopCenter),
                verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    modifier = Modifier.fillMaxSize().weight(0.9f).padding(15.dp),
                    state = pagerState,
                    pageSpacing = 20.dp,
                    pageSize = PageSize.Fill,
                    beyondViewportPageCount = tutorialSteps.size,
                    userScrollEnabled = false
                ) { index ->
                    tutorialSteps[index].content()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.offset(y = -(16).dp).wrapContentWidth().height(50.dp).clip(RoundedCornerShape(100))
                        .padding(8.dp)
                        .background(color = Color.White, shape = CircleShape)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 0 && !change.isOutOfBounds(size, extendedTouchPadding)) {
                                    scope.launch {
                                        viewModel.onChangePage()
                                        animateToNextPage(-1)
                                    }
                                } else if (dragAmount < 0 && !change.isOutOfBounds(size, extendedTouchPadding)) {
                                    scope.launch {
                                        viewModel.onChangePage()
                                        animateToNextPage(1)
                                    }
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            scope.launch {
                                viewModel.onChangePage()
                                animateToNextPage(-1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Go back"
                        )
                    }
                    Box(
                        Modifier.fillMaxHeight().width(22.dp * tutorialSteps.size),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        tutorialSteps.forEachIndexed { index, _ ->
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
                                .jumpingDotTransition(pagerState, 0.35f)
                                .size(20.dp)
                                .background(
                                    color = (if (state.completed[state.currentPage]) Color.Green else Color.Red).copy(alpha = 0.7f),
                                    shape = CircleShape,
                                )
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            if (pagerState.currentPage + 1 >= state.totalPages) {
                                viewModel.showCloseDialog(true)
                            }
                            scope.launch {
                                viewModel.onChangePage()
                                animateToNextPage(1)
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
}

private fun Modifier.jumpingDotTransition(pagerState: PagerState, jumpScale: Float) =
    graphicsLayer {
        val pageOffset = pagerState.currentPageOffsetFraction
        val scrollPosition = pagerState.currentPage + pageOffset

        translationX = (scrollPosition * (size.width + 2.dp.roundToPx()))

        val scale: Float
        val targetScale = jumpScale - 1f

        scale = if (pageOffset.absoluteValue < .5) {
            1.0f + (pageOffset.absoluteValue * 2) * targetScale
        } else {
            jumpScale + ((1 - (pageOffset.absoluteValue * 2)) * targetScale);
        }

        scaleX = scale
        scaleY = scale
    }

