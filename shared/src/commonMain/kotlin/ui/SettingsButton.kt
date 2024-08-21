package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.placeholder_icon
import org.jetbrains.compose.resources.vectorResource
import theme.defaultTextStyle
import theme.generateShadow
import theme.scaledSp
import theme.textShadowStyle

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageVector: ImageVector = vectorResource(Res.drawable.placeholder_icon),
    text: String = "",
    textSizeMultiplier: Float = 1f,
    mainColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    visible: Boolean = true,
    shadowEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    overlay: @Composable () -> Unit = {}
) {
    val matrix = remember(mainColor) {
        ColorMatrix().apply {
            setToScale(
                mainColor.red, mainColor.green, mainColor.blue, mainColor.alpha
            )
        }
    }
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier
        .alpha(if (visible) 1f else 0f)
        .aspectRatio(1.0f)
        .clip(shape)
        .background(backgroundColor)
        .then(if (enabled && visible) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPress()
                    if (hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }, onTap = {
                    onTap()
                }, onLongPress = {
                    onLongPress()
                }, onDoubleTap = {
                    onDoubleTap()
                })
            }
        } else {
            Modifier
        })) {
        val fontSize = (maxWidth / 9f).value.scaledSp
        val fontPadding = (maxWidth / 5.5f)
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(0.5f)
            ) {
                ImageWithShadow(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = imageVector,
                    contentDescription = "settings button image",
                    colorFilter = ColorFilter.colorMatrix(matrix),
                    shadowColor = generateShadow(),
                    shadowEnabled = shadowEnabled
                )
                overlay()
            }
            if (text.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth(),
                    text = text,
                    color = mainColor,
                    fontSize = fontSize * textSizeMultiplier,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = if (shadowEnabled) textShadowStyle() else defaultTextStyle(),
                )
//                Spacer(Modifier.height(fontPadding))
            }
        }
    }
}

@Composable
fun ImageWithShadow(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    shadowEnabled: Boolean = false,
    shadowColor: Color = Color.Black,
    shadowAlpha: Float = 0.7f,
) {
    Box(modifier = modifier.padding(2.dp)) {
        if (shadowEnabled) {
            Image(
                imageVector = imageVector,
                contentDescription = "Shadow",
                alignment = alignment,
                contentScale = contentScale,
                alpha = shadowAlpha,
                colorFilter = ColorFilter.tint(shadowColor),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(1.dp, 1.dp)
                    .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            )
        }

        Image(
            imageVector = imageVector,
            contentDescription = contentDescription,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize()
        )
    }
}


