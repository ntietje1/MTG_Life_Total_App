package domain.storage


interface IImageManager {
    suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String
    fun getImagePath(fileName: String): String?
}

expect class ImageManager : IImageManager {
    override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String
    override fun getImagePath(fileName: String): String?
}