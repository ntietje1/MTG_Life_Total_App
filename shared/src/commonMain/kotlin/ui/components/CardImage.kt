package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.card_back
import org.jetbrains.compose.resources.painterResource
import theme.LocalDimensions

@Composable
fun CardImage(
    modifier: Modifier = Modifier,
    imageUri: String,
    placeholderPainter: Painter = painterResource(Res.drawable.card_back),
    progressIndicatorEnabled: Boolean = false
) {
    val dimensions = LocalDimensions.current
    val painter = asyncPainterResource(imageUri)
    KamelImage(
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        resource = { painter },
        contentDescription = "Player uploaded image",
        onLoading = { progress ->
            Image(
                painter = placeholderPainter,
                contentDescription = "Player uploaded image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
        }, onFailure = {
            Image(
                painter = placeholderPainter,
                contentDescription = "Player uploaded image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}