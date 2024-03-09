package data

import androidx.compose.runtime.Composable

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
     * @param uri the path to the temporarily available image
     * @param fileName the name to use for the file
     * @return the path to the image in the app's local storage
     */
    suspend fun copyImageToLocalStorage(uri: String, fileName: String): String

}