package ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import theme.halfAlpha
import theme.scaledSp

@Composable
fun ResetButton(modifier: Modifier = Modifier, onReset: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(2.5f).clip(RoundedCornerShape(15)).pointerInput(Unit) {
            detectTapGestures(onTap = { _ -> onReset() })
        },

        ) {
        val textSize = remember(Unit) { (maxWidth / 4f).value }
        val textPadding = remember(Unit) { maxHeight / 9f }
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onSurface.halfAlpha()
        ) {
            Text(
                text = "Reset",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = textSize.scaledSp),
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = textPadding)
            )
        }
    }
}