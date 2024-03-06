package data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import com.hypeapps.lifelinked.R
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Initializes the ImageStorage class with platform-specific implementations
 * @return The initialized ImageStorage
 */
@Composable
actual fun initImageManager(): ImageStorage {
    return ImageStorage().apply {
        setContext(LocalContext.current)
    }
}

/**
 * Actual implementation of ImageStorage on android
 */
actual class ImageStorage {
    private lateinit var context: Context

    /**
     * Sets the context for the ImageStorage to access internal storage
     */
    fun setContext(context: Context) {
        this.context = context
    }

    /**
     * Saves an image to the platform-specific app storage
     * @param bytes The image data to save
     * @param name The name of the image
     * @param extension The extension of the image
     * @return The path to the saved image
     */
    actual suspend fun saveImage(bytes: ByteArray, name: String, extension: String): String {
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
    actual suspend fun getByteArrayFromLocalUri(uri: String): ByteArray {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val res = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()
        return res
    }

    /**
     * Retrieves an image over http
     * @param url The url to retrieve the image from
     * @return The image as a ByteArray
     */
    actual suspend fun getByteArrayFromHttp(url: String): ByteArray {
        val client = HttpClient()
        val response = client.request(url)
        return response.bodyAsChannel().toByteArray()
    }

    /**
     * Converts a ByteArray to bitmap
     * @param bytes The image data to convert
     * @param reqWidth The width of the image (in pixels)
     * @param reqHeight The height of the image (in pixels)
     * @return The image as an ImageBitmap
     */
    actual suspend fun imageBitmapFromBytes(bytes: ByteArray, reqWidth: Int, reqHeight: Int): ImageBitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options).asImageBitmap()
    }

    /**
     * Calculates the down sample amount for an image based on visible size
     * @param options The options for the image
     * @param reqWidth The width of the image (in pixels)
     * @param reqHeight The height of the image (in pixels)
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}