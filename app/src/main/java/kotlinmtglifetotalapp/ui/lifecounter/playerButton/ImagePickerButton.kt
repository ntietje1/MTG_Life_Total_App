import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet

class ImagePickerButton(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatButton(context, attrs) {

    private var backgroundType: String = "color"
    private var backgroundImageUri: Uri? = null
    private var backgroundColor: Int = defaultBackgroundColor

    private fun setButtonBackground() {
        if (backgroundType == "image" && backgroundImageUri != null) {
            // Load and set the image as the background
//            val backgroundBitmap: Bitmap = loadBackgroundImageFromUri(backgroundImageUri)
//            setBackgroundBitmap(backgroundBitmap)
        } else {
            // Set the background color
            setBackgroundColor(backgroundColor)
        }
    }

    fun setBackgroundType(type: String) {
        backgroundType = type
        setButtonBackground()
    }

    fun setBackgroundImage(imageUri: Uri) {
        backgroundImageUri = imageUri
        setButtonBackground()
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        setButtonBackground()
    }

    companion object {
        const val defaultBackgroundColor: Int = 0
    }
}
