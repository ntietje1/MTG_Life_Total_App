package theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Typeface
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface

private fun loadCustomFont(name: String, fontStyle: FontStyle): Typeface {
    return Typeface.makeFromName(name, fontStyle)
}

actual fun robotoFontFamily(): FontFamily = FontFamily(
    Typeface(loadCustomFont("roboto", FontStyle.NORMAL))
)

