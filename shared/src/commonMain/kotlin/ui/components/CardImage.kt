package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.card_back
import org.jetbrains.compose.resources.painterResource
import theme.LocalDimensions

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