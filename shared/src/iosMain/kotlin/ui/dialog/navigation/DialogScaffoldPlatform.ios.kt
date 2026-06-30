package ui.dialog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

internal actual fun dialogScaffoldProperties(): DialogProperties =
    DialogProperties(
        dismissOnBackPress = false,
        usePlatformDefaultWidth = false,
    )

@Composable
internal actual fun dialogScaffoldTopPadding(): Dp = 0.dp
