package theme


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import kotlin.math.min

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

val normalColorMatrix = ColorMatrix().generateColorMatrix(1.0f, 1.0f)

val receiverColorMatrix = ColorMatrix().generateColorMatrix(0.0f, 0.3f)

val dealerColorMatrix = ColorMatrix().generateColorMatrix(0.6f, 0.4f)

val settingsColorMatrix = ColorMatrix().generateColorMatrix(0.8f, 0.6f)

val deadNormalColorMatrix = ColorMatrix().generateColorMatrix(1.0f, 1.0f, true)

val deadReceiverColorMatrix = ColorMatrix().generateColorMatrix(0.0f, 0.3f, true)

val deadDealerColorMatrix = ColorMatrix().generateColorMatrix(0.6f, 0.4f, true)

val deadSettingsColorMatrix = ColorMatrix().generateColorMatrix(0.8f, 0.6f, true)

fun Color.ghostify(): Color {
    return this.copy().blendWith(Color.Gray)
}

fun ColorMatrix.generateColorMatrix(sat: Float, lum: Float, dead: Boolean = false): ColorMatrix {
    val s = if (dead) sat * 0.2f else sat
    val l = if (dead) lum * 1.05f else lum
    val a = 1.0f
    return this.apply {
        timesAssign(ColorMatrix().apply { setToSaturation(s) })
        timesAssign(ColorMatrix().apply { setToScale(l, l, l, a) })
    }
}

fun generateShadow(): Color {
//    return this.invert().saturateColor(0.0f).brightenColor(0.6f).copy(alpha = 0.7f)
    return Color.Black.copy(alpha = 0.5f)
}

fun Color.blendWith(other: Color): Color {
    val alpha = 0.5f
    val blendedRed = (1 - alpha) * this.red + alpha * other.red
    val blendedGreen = (1 - alpha) * this.green + alpha * other.green
    val blendedBlue = (1 - alpha) * this.blue + alpha * other.blue

    return Color(blendedRed, blendedGreen, blendedBlue)
}

fun Color.brightenColor(factor: Float): Color {
    return this.toHsv().let {
        Color.hsv(it[0], it[1], min(it[2] * factor, 1.0f))
    }
}

fun Color.saturateColor(factor: Float): Color {
    return this.toHsv().let {
        Color.hsv(it[0], it[1] * factor, it[2])
    }
}

//fun Color.brightenColor(factor: Float): Color {
//    return this.toHsv().let {
//        Color.hsv(it[0], it[1], min(it[2] + (1.0f - it[2]) * factor, 1.0f))
//    }
//}
//
//fun Color.saturateColor(factor: Float): Color {
//    return this.toHsv().let {
//        Color.hsv(it[0], it[1] + (1.0f - it[1]) * factor, it[2])
//    }
//}

//fun Color.invert(): Color {
//    return this.copy(red = 1f - this.red, green = 1f - this.green, blue = 1f - this.blue)
//}

fun Color.toHsv(): FloatArray {
    val r = this.red
    val g = this.green
    val b = this.blue

    val maxc = maxOf(r, g, b)
    val minc = minOf(r, g, b)
    val v = maxc

    if (minc == maxc) {
        return floatArrayOf(0.0f, 0.0f, v)
    }

    val s = (maxc - minc) / maxc
    val rc = (maxc - r) / (maxc - minc)
    val gc = (maxc - g) / (maxc - minc)
    val bc = (maxc - b) / (maxc - minc)

    val h = ((when {
        r == maxc -> bc - gc
        g == maxc -> 2.0f + rc - bc
        else -> 4.0f + gc - rc
    } / 6.0f % 1.0f) * 360f).coerceIn(0.0f..360.0f)

    return floatArrayOf(h, s, v)
}

