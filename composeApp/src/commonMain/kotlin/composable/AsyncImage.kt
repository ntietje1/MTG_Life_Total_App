package composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import data.ImageStorage
import data.initImageManager

/**
 * A composable that loads an image asynchronously
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex. background)
 * @param uri The uri of the image to load (local or http).
 * @param contentDescription text used by accessibility services to describe what this image represents. This should always be provided unless this image is used for decorative purposes, and does not represent a meaningful action that a user can take. This text should be localized, such as by using androidx.compose.ui.res.stringResource or similar
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used if the bounds are a different size from the intrinsic size of the Painter
 * @param alpha Optional opacity to be applied to the Painter when it is rendered onscreen the default renders the Painter completely opaque
 * @param colorFilter Optional colorFilter to apply for the Painter when it is rendered onscreen
 */
@Composable
fun AsyncImage(
    modifier: Modifier = Modifier, uri: String,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null

) {
    val imageStorage = initImageManager()
    var painter: Painter? by remember { mutableStateOf(null) }
    BoxWithConstraints(modifier = modifier) {
        LaunchedEffect(uri) {
            val imageByteArray = if (uri.startsWith("http")) {
                imageStorage.getByteArrayFromHttp(uri)
            } else {
                imageStorage.getByteArrayFromLocalUri(uri)
            }
            val imageBitmap = imageByteArray.let { imageStorage.imageBitmapFromBytes(it, maxWidth.value.toInt(), maxHeight.value.toInt()) }
            painter = BitmapPainter(imageBitmap)
        }
        if (painter != null) {
            Image(
                painter = painter!!,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        }
    }
}