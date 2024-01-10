package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
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
    throw NotImplementedError()
}

/**
 * Actual implementation of ImageStorage on IOS
 */
actual class ImageStorage {

    private val fileManager = NSFileManager.defaultManager
    private val documentDirectory = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).first() as NSString

    actual suspend fun saveImage(bytes: ByteArray, name: String, extension: String): String {
        return withContext(Dispatchers.Default) {
            val fileName = name + NSUUID.UUID().UUIDString + ".jpg"
            val fullPath = documentDirectory.stringByAppendingPathComponent(fileName)

            val data = bytes.usePinned {
                NSData.create(
                    bytes = it.addressOf(0),
                    length = bytes.size.toULong()
                )
            }

            data.writeToFile(
                path = fullPath,
                atomically = true
            )
            fullPath
        }
    }

//    actual suspend fun getImage(fileName: String): ByteArray? {
//        return withContext(Dispatchers.Default) {
//            memScoped {
//                NSData.dataWithContentsOfFile(fileName)?.let { bytes ->
//                    val array = ByteArray(bytes.length.toInt())
//                    bytes.getBytes(array.refTo(0).getPointer(this), bytes.length)
//                    return@withContext array
//                }
//            }
//            return@withContext null
//        }
//    }

    actual suspend fun deleteImage(fileName: String) {
        withContext(Dispatchers.Default) {
            fileManager.removeItemAtPath(fileName, null)
        }
    }

    actual suspend fun imageBitmapFromBytes(bytes: ByteArray, reqWidth: Int, reqHeight: Int): ImageBitmap {
        throw NotImplementedError()
    }

    actual suspend fun getByteArrayFromLocalUri(uri: String): ByteArray {
        throw NotImplementedError()
    }

}