package data

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*

actual class ImageManager {
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