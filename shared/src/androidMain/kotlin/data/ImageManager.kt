package data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

actual class ImageManager(private val context: Context) {
    private suspend fun saveImage(bytes: ByteArray, name: String): String {
        return withContext(context = Dispatchers.IO) {
//            val fileName = "$name${UUID.randomUUID()}.$extension" // generating a new "random" name prevents an issue with updating the loaded image
            //TODO: come up with better solution for this
            val fileName = "$name-${UUID.randomUUID()}"
//            val fileName = name
//            val file = File(context.filesDir, fileName)
//            if (file.exists()) {
//                file.delete()
//            }
            delay(10)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(bytes)
            }
            fileName
//            FileProvider.getUriForFile(
//                context, ContextCompat.getString(context, R.string.file_provider_authority), File(context.filesDir, fileName)
//            ).toString()
        }
    }

//    private fun getByteArrayFromLocalUri(uri: String): ByteArray {
//        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
//        val res = inputStream?.readBytes() ?: ByteArray(0)
//        inputStream?.close()
//        return res
//    }

//    actual suspend fun copyImageToLocalStorage(uri: String, fileName: String): String {
//        val imageBytes = getByteArrayFromLocalUri(uri)
//        return saveImage(bytes = imageBytes, name = fileName, extension = ".png")
//    }

    actual suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        return saveImage(bytes = bytes, name = fileName)
    }

    actual fun getImagePath(fileName: String): String? {
        val filesDir = context.filesDir
        val files = filesDir.listFiles()
        if (files != null) {
            for (file in files) {
                println("GOT FILE: ${file.name}")
                if (file.name.startsWith(fileName)) {
                    return file.absolutePath
                }
            }
        }
        return null
//        throw FileNotFoundException("No file found with name starting with $fileName")
    }
}