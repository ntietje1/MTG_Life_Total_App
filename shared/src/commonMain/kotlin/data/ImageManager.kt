package data


expect class ImageManager {
    suspend fun copyImageToLocalStorage(uri: String, fileName: String): String
}