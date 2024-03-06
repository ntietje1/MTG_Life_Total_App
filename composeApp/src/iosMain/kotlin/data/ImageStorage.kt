package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile

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
     * Saves an image to the platform-specific app storage
     * @param bytes The image data to save
     * @param name The name of the image
     * @param extension The extension of the image
     * @return The path to the saved image
     */
    actual suspend fun saveImage(bytes: ByteArray, name: String, extension: String): String {
        throw NotImplementedError()
    }

    /**
     * Retrieves an image from the platform-specific storage
     * @param uri The uri of the image to retrieve
     * @return The image as a ByteArray
     */
    actual suspend fun getByteArrayFromLocalUri(uri: String): ByteArray {
        throw NotImplementedError()
    }

    /**
     * Retrieves an image over http
     * @param url The url to retrieve the image from
     * @return The image as a ByteArray
     */
    actual suspend fun getByteArrayFromHttp(url: String): ByteArray {
        throw NotImplementedError()
    }

    /**
     * Converts a ByteArray to bitmap
     * @param bytes The image data to convert
     * @param reqWidth The width of the image (in pixels)
     * @param reqHeight The height of the image (in pixels)
     * @return The image as an ImageBitmap
     */
    actual suspend fun imageBitmapFromBytes(bytes: ByteArray, reqWidth: Int, reqHeight: Int): ImageBitmap {
        throw NotImplementedError()
    }

}