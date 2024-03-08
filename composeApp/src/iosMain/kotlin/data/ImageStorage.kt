package data

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*

/**
 * Initializes the ImageStorage class with platform-specific implementations
 * @return The initialized ImageStorage
 */
@Composable
actual fun initImageManager(): ImageStorage {
    return ImageStorage()
}

/**
 * Actual implementation of ImageStorage on IOS
 */
actual class ImageStorage {

    /**
     * @param uri the path to the temporarily available image
     * @param fileName the name to use for the file
     * @return the path to the image in the app's local storage
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun copyImageToLocalStorage(uri: String, fileName: String): String {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: throw IllegalStateException("Document directory not found.")

        val destinationPath = "$documentsDir/$fileName"

        val fileManager = NSFileManager.defaultManager
        val sourceURL = NSURL.fileURLWithPath(uri)
        val destinationURL = NSURL.fileURLWithPath(destinationPath)
        val copyResult = fileManager.copyItemAtURL(sourceURL, destinationURL, null)

        return if (copyResult) destinationPath else throw IllegalStateException("Failed to copy image to local storage.")

    }
}