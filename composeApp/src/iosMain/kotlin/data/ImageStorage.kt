package data

import androidx.compose.runtime.Composable

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
     * @param extension the file extension of the image
     * @return the path to the image in the app's local storage
     */
    actual suspend fun copyImageToLocalStorage(uri: String, fileName: String): String {
        TODO("Not yet implemented")
    }


}