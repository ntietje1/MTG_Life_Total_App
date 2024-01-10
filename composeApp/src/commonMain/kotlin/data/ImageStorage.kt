package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Initializes the ImageStorage class with platform-specific implementations
 * @return The initialized ImageStorage
 */
@Composable
expect fun initImageManager(): ImageStorage

/**
 * Expect class for ImageStorage, Implemented in the platform-specific modules
 */
expect class ImageStorage private constructor() {
    /**
     * Saves an image to the platform-specific storage
     * @param bytes The image data to save
     * @param name The name of the image
     * @param extension The extension of the image
     * @return The path to the saved image
     */
    suspend fun saveImage(bytes: ByteArray, name: String, extension: String): String

    /**
     * Deletes an image from the platform-specific storage
     * @param fileName The name of the image to delete
     */
    suspend fun deleteImage(fileName: String)

    /**
     * Retrieves an image from the platform-specific storage
     * @param bytes The image data to convert
     * @param reqWidth The width of the image (in pixels)
     * @param reqHeight The height of the image (in pixels)
     * @return The image as an ImageBitmap
     */
    suspend fun imageBitmapFromBytes(bytes: ByteArray, reqWidth: Int, reqHeight: Int): ImageBitmap

    /**
     * Retrieves an image over http
     * @param url The url to retrieve the image from
     * @return The image as a ByteArray
     */
    suspend fun getByteArrayFromHttp(url: String): ByteArray

    /**
     * Retrieves an image from the platform-specific storage
     * @param uri The uri of the image to retrieve
     * @return The image as a ByteArray
     */
    suspend fun getByteArrayFromLocalUri(uri: String): ByteArray
}