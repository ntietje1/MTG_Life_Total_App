package lifelinked.data

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat.getString
import androidx.core.content.FileProvider
import com.hypeapps.lifelinked.R
import java.io.File
import java.io.IOException

class ImageManager(private val context: Context, private val player: Player) {

    fun copyImageToInternalStorage(uri: Uri): Uri? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val currentTime = System.currentTimeMillis()
            val fileName = "${player.name}_${currentTime}_background.jpg"

            // Delete files starting with the same player name prefix
            context.filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith(player.name) && file.name != fileName) {
                    file.delete()
                }
            }

            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return FileProvider.getUriForFile(
                context, getString(context, R.string.file_provider_authority), File(context.filesDir, fileName)
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}