package theme


import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface

private fun loadCustomFont(name: String, fontStyle: FontStyle): Typeface {
    return Typeface.makeFromName(name, fontStyle)
}

