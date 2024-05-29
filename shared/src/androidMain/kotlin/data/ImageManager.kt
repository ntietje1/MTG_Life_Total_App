package data

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.hypeapps.lifelinked.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

actual class ImageManager(private val context: Context) {
    private suspend fun saveImage(bytes: ByteArray, name: String, extension: String): String {
        return withContext(context = Dispatchers.IO) {
            val fileName = "$name${UUID.randomUUID()}.$extension"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(bytes)
            }
            FileProvider.getUriForFile(
                context, ContextCompat.getString(context, R.string.file_provider_authority), File(context.filesDir, fileName)
            ).toString()
        }
    }

    private fun getByteArrayFromLocalUri(uri: String): ByteArray {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val res = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()
        return res
    }

    actual suspend fun copyImageToLocalStorage(uri: String, fileName: String): String {
        val imageBytes = getByteArrayFromLocalUri(uri)
        return saveImage(bytes = imageBytes, name = fileName, extension = ".png")
    }

    actual suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        return saveImage(bytes = bytes, name = fileName, extension = ".png")
    }
}