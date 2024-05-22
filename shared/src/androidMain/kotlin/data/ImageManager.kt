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

/**
 * Actual implementation of ImageStorage on android
 */
actual class ImageManager(private val context: Context) {

    /**
     * Saves an image to the platform-specific app storage
     * @param bytes The image data to save
     * @param name The name of the image
     * @param extension The extension of the image
     * @return The path to the saved image
     */
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

    /**
     * Retrieves an image from the platform-specific storage
     * @param uri The uri of the image to retrieve
     * @return The image as a ByteArray
     */
    private fun getByteArrayFromLocalUri(uri: String): ByteArray {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val res = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()
        return res
    }

    /**
     * @param uri the path to the temporarily available image
     * @param fileName the name to use for the file
     * @param extension the file extension of the image
     * @return the path to the image in the app's local storage
     */
    actual suspend fun copyImageToLocalStorage(uri: String, fileName: String): String {
        val imageBytes = getByteArrayFromLocalUri(uri)
        return saveImage(bytes = imageBytes, name = fileName, extension = ".png")
    }

}