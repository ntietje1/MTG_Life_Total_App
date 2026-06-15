package ui.dialog.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon_alt
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import ui.components.SettingsButton

@Composable
fun DialogScaffold(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    exitButtonEnabled: Boolean = true,
    backButtonEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val buttonSize = remember(Unit) { maxWidth / 6.5f }

            Column(Modifier.fillMaxSize()) {
                if (exitButtonEnabled) {
                    Row(
                        Modifier.fillMaxWidth().wrapContentHeight(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DialogIconButton(
                            modifier = Modifier.size(buttonSize),
                            visible = true,
                            icon = Res.drawable.x_icon,
                            contentDescription = "Close dialog",
                            onPress = onDismiss
                        )
                    }
                }
                Box(
                    Modifier.weight(0.1f).padding(5.dp)
                ) {
                    content()
                }

                if (backButtonEnabled) {
                    Row(
                        Modifier.fillMaxWidth().wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DialogIconButton(
                            modifier = Modifier.size(buttonSize),
                            visible = true,
                            icon = Res.drawable.back_icon_alt,
                            contentDescription = "Back in dialog",
                            onPress = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogIconButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    icon: org.jetbrains.compose.resources.DrawableResource,
    contentDescription: String,
    onPress: () -> Unit
) {
    SettingsButton(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageVector = vectorResource(icon),
        contentDescription = contentDescription,
        onPress = onPress
    )
}
