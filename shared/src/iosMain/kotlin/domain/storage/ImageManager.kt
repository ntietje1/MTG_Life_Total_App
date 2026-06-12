package domain.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

actual class ImageManager: IImageManager {
    actual override fun getImagePath(fileName: String): String? {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull()
        if (documentsDir == null) {
            println("Document directory not found.")
            return null
        }
        return "$documentsDir/$fileName"
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: throw IllegalStateException("Document directory not found.")

        val destinationPath = "$documentsDir/$fileName"

        val file = fopen(destinationPath, "wb")
            ?: throw IllegalStateException("Failed to open image file for writing.")

        val byteCount = bytes.size.toULong()
        val written = bytes.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1uL, byteCount, file)
        }
        fclose(file)
        if (written != byteCount) {
            throw IllegalStateException("Failed to write image data to file.")
        }

        return fileName
    }
}
