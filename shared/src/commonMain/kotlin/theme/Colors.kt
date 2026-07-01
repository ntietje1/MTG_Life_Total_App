package theme


import androidx.compose.ui.graphics.Color
import domain.state.profile.PlayerColors as DomainPlayerColors
import kotlin.math.max
import kotlin.math.min

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val MainColor = Color(252, 76, 97)
val MainColorLight = Color(247, 101, 162)

val PlayerColor1 = Color(DomainPlayerColors.DefaultPalette[0].backgroundArgb)
val PlayerColor2 = Color(DomainPlayerColors.DefaultPalette[1].backgroundArgb)
val PlayerColor3 = Color(DomainPlayerColors.DefaultPalette[2].backgroundArgb)
val PlayerColor4 = Color(DomainPlayerColors.DefaultPalette[3].backgroundArgb)
val PlayerColor5 = Color(DomainPlayerColors.DefaultPalette[4].backgroundArgb)
val PlayerColor6 = Color(DomainPlayerColors.DefaultPalette[5].backgroundArgb)
val PlayerColor7 = Color(DomainPlayerColors.DefaultPalette[6].backgroundArgb)
val PlayerColor8 = Color(DomainPlayerColors.DefaultPalette[7].backgroundArgb)
val PlayerColor9 = Color(DomainPlayerColors.DefaultPalette[8].backgroundArgb)

fun Color.ghostify(): Color {
    return this.copy().blendWith(Color.Gray)
}

fun Color.halfAlpha(): Color {
    return this.copy(alpha = this.alpha / 2f)
}

fun generateShadow(): Color {
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

fun Color.toHsv(): FloatArray {
    val r = this.red
    val g = this.green
    val b = this.blue

    val cmax = max(r, max(g, b))
    val cmin = min(r, min(g, b))
    val diff = cmax - cmin

    val h = when {
        cmax == cmin -> 0.0f
        cmax == r -> (60 * ((g - b) / diff) + 360) % 360
        cmax == g -> (60 * ((b - r) / diff) + 120) % 360
        else -> (60 * ((r - g) / diff) + 240) % 360
    }

    val s = if (cmax == 0.0f) 0.0f else diff / cmax
    val v = cmax

    return floatArrayOf(h, s, v)
}

