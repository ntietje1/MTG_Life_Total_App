package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.card_back
import org.jetbrains.compose.resources.painterResource
import theme.LocalDimensions
import ui.modifier.routePointerChangesTo

const val CARD_CORNER_PERCENT = 7

@Composable
fun CardImage(
    modifier: Modifier = Modifier,
    painter: Resource<Painter>,
    placeholderPainter: Resource<Painter> = asyncPainterResource(Res.drawable.card_back),
    progressIndicatorEnabled: Boolean = false
) {
    val dimensions = LocalDimensions.current

    KamelImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        resource = { painter },
        contentDescription = "Player uploaded image",
        onLoading = { progress ->
            Box(modifier = Modifier.fillMaxSize()) {
                KamelImage(
                    resource = { placeholderPainter },
                    contentDescription = "Placeholder image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (progressIndicatorEnabled) {
                    if (progress == 0.0f) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = dimensions.borderSmall,
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { progress },
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = dimensions.borderSmall,
                        )
                    }
                }
            }
        },
        onFailure = {
            KamelImage(
                resource = { placeholderPainter },
                contentDescription = "Placeholder image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
fun CardImage(
    modifier: Modifier = Modifier,
    painter: Resource<Painter>,
    placeholderPainter: Painter,
    progressIndicatorEnabled: Boolean = false
) {
    CardImage(
        modifier = modifier,
        painter = painter,
        placeholderPainter = Resource.Success(placeholderPainter),
        progressIndicatorEnabled = progressIndicatorEnabled
    )
}

@Composable
fun CardImage(
    modifier: Modifier = Modifier,
    imageUri: String,
    placeholderPainter: Painter = painterResource(Res.drawable.card_back),
    progressIndicatorEnabled: Boolean = false
) = CardImage(
    modifier = modifier,
    painter = asyncPainterResource(imageUri),
    placeholderPainter = placeholderPainter,
    progressIndicatorEnabled = progressIndicatorEnabled
)


@Composable
fun EnlargeableCardImage(
    modifier: Modifier = Modifier,
    smallImageUri: String,
    largeImageUri: String,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    allowRotate: Boolean = false,
    allowEnlarge: Boolean = true,
) = SelectableEnlargeableCardImage(
    modifier = modifier,
    normalImageUri = smallImageUri,
    largeImageUri = largeImageUri,
    allowSelection = false,
    onPress = onPress,
    onTap = onTap,
    allowRotate = allowRotate,
    allowEnlarge = allowEnlarge,
    selected = false,
    showSelectedBackground = false
)

@Composable
fun SelectableEnlargeableCardImage(
    modifier: Modifier = Modifier,
    normalImageUri: String,
    largeImageUri: String,
    allowSelection: Boolean = false,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    allowEnlarge: Boolean = true,
    allowRotate: Boolean = false,
    selected: Boolean = false,
    showSelectedBackground: Boolean = true
) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    var longPressed by remember { mutableStateOf(false) }
    var rotated by remember { mutableStateOf(false) }

    if (showLargeImage && allowEnlarge) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                routePointerChangesTo(
                    onUp = {
                        if (!longPressed) {
                            showLargeImage = false
                        }
                        longPressed = false
                    },
                    onLongPress = {
                        if (allowRotate) {
                            delay(500)
                            longPressed = true
                            rotated = !rotated
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                )
            }) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize().padding(dimensions.paddingSmall)
                ) {
                    val cardAspectRatio = 5f / 7f // Standard card ratio
                    val screenAspectRatio = maxWidth / maxHeight

                    val largeImageModifier = if (screenAspectRatio > cardAspectRatio) {
                        // Screen is wider than card - constrain by height
                        Modifier.fillMaxHeight().aspectRatio(cardAspectRatio)
                    } else {
                        // Screen is taller than card - constrain by width
                        Modifier.fillMaxWidth().aspectRatio(cardAspectRatio)
                    }

                    CardImage(
                        modifier = largeImageModifier
                            .clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                            .align(Alignment.Center)
                            .graphicsLayer { rotationZ = if (rotated) 180f else 0f },
                        painter = asyncPainterResource(largeImageUri),
                        placeholderPainter = painterResource(Res.drawable.card_back)
                    )
                }
            }
        })
    }

    BoxWithConstraints(modifier = modifier.aspectRatio(5 / 7f).pointerInput(Unit) {
        detectTapGestures(onLongPress = {
            if (allowEnlarge) {
                showLargeImage = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }, onPress = {
            onPress()
        }, onTap = {
            if (allowSelection) {
                onTap()
            }
        })
    }) {
        Box(
            Modifier.fillMaxSize().then(
                if (!showSelectedBackground) {
                    Modifier
                } else if (selected) {
                    Modifier.background(Color.Green.copy(alpha = 0.2f))
                } else {
                    Modifier.background(Color.Red.copy(alpha = 0.1f))
                }
            )
        ) {
            CardImage(
                modifier = Modifier.fillMaxSize().then(
                    if (selected) {
                        Modifier.padding(dimensions.paddingSmall).clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                    } else {
                        Modifier.padding(dimensions.paddingTiny).clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                    }
                ), imageUri = normalImageUri
            )
        }

    }
}