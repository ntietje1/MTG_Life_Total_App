package lifelinked.ui.theme


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.core.graphics.ColorUtils
import java.lang.Float.min

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)


val PlayerColor1 = Color(0xFFf289e2)
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
    val s = if (dead) sat * 0.2f else sat
    val l = if (dead) lum * 1.2f else lum
    val a = if (dead) 0.7f else 1.0f
    return this.apply {
        timesAssign(ColorMatrix().apply { setToSaturation(s) })
        timesAssign(ColorMatrix().apply { setToScale(l, l, l, a) })
    }
}

fun generateShadow(): Color {
//    return this.invert().saturateColor(0.0f).brightenColor(0.6f).copy(alpha = 0.7f)
    return Color.Black.copy(alpha = 0.7f)
}

fun Color.blendWith(other: Color): Color {
    val alpha = 0.5f
    val blendedRed = (1 - alpha) * this.red + alpha * other.red
    val blendedGreen = (1 - alpha) * this.green + alpha * other.green
    val blendedBlue = (1 - alpha) * this.blue + alpha * other.blue

    return Color(blendedRed, blendedGreen, blendedBlue)
}

fun Color.brightenColor(factor: Float): Color {
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    val hsl = FloatArray(3)
    ColorUtils.RGBToHSL(r, g, b, hsl)
    hsl[2] *= factor
    hsl[2] = min(1.0f, hsl[2])
    return Color.hsl(hsl[0], hsl[1], hsl[2])
}

fun Color.saturateColor(factor: Float): Color {
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    val hsl = FloatArray(3)
    ColorUtils.RGBToHSL(r, g, b, hsl)
    hsl[1] *= factor
    hsl[1] = min(1.0f, hsl[1])
    return Color.hsl(hsl[0], hsl[1], hsl[2])
}


fun Color.invert(): Color {
    return this.copy(red = 1f - this.red, green = 1f - this.green, blue = 1f - this.blue)
}

