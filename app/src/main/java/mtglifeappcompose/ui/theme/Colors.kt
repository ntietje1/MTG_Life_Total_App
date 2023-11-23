package mtglifeappcompose.ui.theme


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils


val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val White = Color(0xFFFFFFFF)
val Gold = Color(255, 191, 8)


val PlayerColor1 = Color(0xFFF75FA8)
val PlayerColor2 = Color(0xFFee4c87)
val PlayerColor3 = Color(0xFFF75F5F)
val PlayerColor4 = Color(0xFFf78e55)
val PlayerColor5 = Color(0xFFF7C45F)
val PlayerColor6 = Color(0xFF44da5c)
val PlayerColor7 = Color(0xFF46e7db)
val PlayerColor8 = Color(0xFF625FF7)
val PlayerColor9 = Color(0xFFc28efc)

val allPlayerColors = listOf(
    PlayerColor1,
    PlayerColor2,
    PlayerColor3,
    PlayerColor4,
    PlayerColor5,
    PlayerColor6,
    PlayerColor7,
    PlayerColor8,
    PlayerColor9
)

val normalColorMatrix = ColorMatrix().generateColorMatrix(1.0f, 1.0f)

val receiverColorMatrix = ColorMatrix().generateColorMatrix(0.0f, 0.3f)

val dealerColorMatrix = ColorMatrix().generateColorMatrix(0.6f, 0.4f)

val settingsColorMatrix = ColorMatrix().generateColorMatrix(0.8f, 0.6f)

val deadNormalColorMatrix = ColorMatrix().generateColorMatrix(1.0f, 1.0f, true)

val deadReceiverColorMatrix = ColorMatrix().generateColorMatrix(0.0f, 0.3f, true)

val deadDealerColorMatrix = ColorMatrix().generateColorMatrix(0.6f, 0.4f, true)

val deadSettingsColorMatrix = ColorMatrix().generateColorMatrix(0.8f, 0.6f, true)

fun ColorMatrix.generateColorMatrix(sat: Float, lum: Float, dead: Boolean = false): ColorMatrix {
    val s = if (dead) sat * 0.3f else sat
    val l = if (dead) lum * 1.1f else lum
    return this.apply {
        timesAssign(ColorMatrix().apply { setToSaturation(s) })
        timesAssign(ColorMatrix().apply { setToScale(l, l, l, 1.0f) })
    }
}

fun Color.darkenColor(factor: Float = 0.6f): Color {
    return Color(this.toArgb().darkenColor(factor))
}

fun Color.desaturateColor(factor: Float = 0.6f): Color {
    return Color(this.toArgb().desaturateColor(factor))
}

fun Int.darkenColor(factor: Float = 0.6f): Int {
    val red = android.graphics.Color.red(this) * factor
    val green = android.graphics.Color.green(this) * factor
    val blue = android.graphics.Color.blue(this) * factor
    return android.graphics.Color.rgb(red.toInt(), green.toInt(), blue.toInt())
}

fun Int.desaturateColor(factor: Float = 0.6f): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this, hsl)
    hsl[1] *= factor
    return ColorUtils.HSLToColor(hsl)
}
