package ui.dialog.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

internal actual fun dialogScaffoldProperties(): DialogProperties =
    DialogProperties(
        dismissOnBackPress = false,
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false,
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal actual fun dialogScaffoldTopPadding(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp() }
}
