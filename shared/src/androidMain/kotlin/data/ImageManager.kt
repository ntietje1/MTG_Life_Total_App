package data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ImageManager(private val context: Context) : IImageManager {
    private suspend fun saveImage(bytes: ByteArray, name: String): String {
        return withContext(context = Dispatchers.IO) {
            val fileName = "$name-${getNextNumber(name)}"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(bytes)
            }
            fileName.also { println("Saved image $it") }
        }
    }

    private fun getNextNumber(name: String): Int {
        val filesDir = context.filesDir
        val files = filesDir.listFiles()
        var res = 1
        if (files != null) {
            val userImages = files.map { it.name }.filter { it.startsWith(name) }
            userImages.forEach {
                context.deleteFile(it)
            }
            val usedNumbers = userImages.map { it.substringAfterLast("-").substringBeforeLast("..") }.mapNotNull { it.toIntOrNull()  }
            val nextNumber = (usedNumbers.maxOrNull() ?: 0) + 1
            res = nextNumber
        }
        return res
    }

    actual override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        return saveImage(bytes = bytes, name = fileName).also {
            println("Saved image to $it")}
    }

    actual override fun getImagePath(fileName: String): String? {
        val filesDir = context.filesDir
        val files = filesDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.name.startsWith(fileName)) {
                    return ("file://" + file.absolutePath).also {
                        println("Found image at $it")
                    }
                }
            }
        }
        return null
    }
}