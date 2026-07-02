package ui.dialog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

internal expect fun dialogScaffoldProperties(): DialogProperties

@Composable
internal expect fun dialogScaffoldTopPadding(): Dp
