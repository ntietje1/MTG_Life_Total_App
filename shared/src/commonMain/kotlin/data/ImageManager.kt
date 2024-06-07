package data


expect class ImageManager {
    suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String
    fun getImagePath(fileName: String): String?
}