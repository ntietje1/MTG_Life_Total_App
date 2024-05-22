package data

/**
 * Expect class for ImageManager, Implemented in the platform-specific modules
 */
expect class ImageManager {

    /**
     * @param uri the path to the temporarily available image
     * @param fileName the name to use for the file
     * @return the path to the image in the app's local storage
     */
    suspend fun copyImageToLocalStorage(uri: String, fileName: String): String
}