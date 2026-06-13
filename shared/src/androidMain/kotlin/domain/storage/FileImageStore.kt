package domain.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

actual class FileImageStore(private val context: Context) : IFileImageStore {
    actual override suspend fun saveImage(bytes: ByteArray): String {
        return withContext(context = Dispatchers.IO) {
            val imageId = "local-image-${UUID.randomUUID()}"
            context.openFileOutput(imageId, Context.MODE_PRIVATE).use {
                it.write(bytes)
            }
            imageId
        }
    }

    actual override fun localImageUri(imageId: String): String? {
        val file = File(context.filesDir, imageId)
        return if (file.exists()) "file://${file.absolutePath}" else null
    }

    actual override fun deleteImage(imageId: String) {
        context.deleteFile(imageId)
    }
}
