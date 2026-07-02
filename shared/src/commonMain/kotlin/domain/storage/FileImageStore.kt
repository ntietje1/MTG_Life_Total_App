package domain.storage

import domain.state.profile.PlayerBackground

interface IFileImageStore {
    suspend fun saveImage(bytes: ByteArray): String
    fun localImageUri(imageId: String): String?
    fun deleteImage(imageId: String)
}

fun PlayerBackground.displayUri(fileImageStore: IFileImageStore): String? {
    return when (this) {
        PlayerBackground.None -> null
        is PlayerBackground.LocalImage -> fileImageStore.localImageUri(fileName)
        is PlayerBackground.ProviderImage -> url
        is PlayerBackground.CardArt -> url
    }
}

expect class FileImageStore : IFileImageStore {
    override suspend fun saveImage(bytes: ByteArray): String
    override fun localImageUri(imageId: String): String?
    override fun deleteImage(imageId: String)
}
