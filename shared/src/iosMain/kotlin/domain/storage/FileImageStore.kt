package domain.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.remove

actual class FileImageStore : IFileImageStore {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun saveImage(bytes: ByteArray): String {
        val imageId = "local-image-${NSUUID().UUIDString}"
        val destinationPath = imagePath(imageId)
            ?: throw IllegalStateException("Document directory not found.")

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

        return imageId
    }

    actual override fun localImageUri(imageId: String): String? {
        val path = imagePath(imageId) ?: return null
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) "file://$path" else null
    }

    actual override fun deleteImage(imageId: String) {
        imagePath(imageId)?.let { path -> remove(path) }
    }

    private fun imagePath(imageId: String): String? {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: return null
        return "$documentsDir/$imageId"
    }
}
