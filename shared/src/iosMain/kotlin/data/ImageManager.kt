package data

import androidx.compose.runtime.Composable
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation

actual class ImageManager {
    actual fun getImagePath(fileName: String): String {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: throw IllegalStateException("Document directory not found.")
        return "$documentsDir/$fileName"
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: throw IllegalStateException("Document directory not found.")

        val destinationPath = "$documentsDir/$fileName"

        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }

        val image = UIImage.imageWithData(nsData)
        val imageData = image?.let { UIImagePNGRepresentation(it) }

        imageData?.writeToFile(destinationPath, atomically = true)
            ?: throw IllegalStateException("Failed to write image data to file.")

        return fileName
    }
}