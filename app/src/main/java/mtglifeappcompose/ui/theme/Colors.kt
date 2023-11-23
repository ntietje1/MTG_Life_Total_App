package mtglifeappcompose.ui.theme

import androidx.compose.ui.graphics.Color
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
val PlayerColor2 = Color(0xFFF75F5F)
val PlayerColor3 = Color(0xFFf78e55)
val PlayerColor4 = Color(0xFFF7C45F)
val PlayerColor5 = Color(0xFF92F75F)
val PlayerColor6 = Color(0xFF409c5a)
val PlayerColor7 = Color(0xFF5FEAF7)
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
