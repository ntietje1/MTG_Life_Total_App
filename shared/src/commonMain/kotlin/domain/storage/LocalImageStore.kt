package domain.storage


interface IImageStore {
    suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String
    fun getImagePath(fileName: String): String?
}

expect class LocalImageStore : IImageStore {
    override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String
    override fun getImagePath(fileName: String): String?
}