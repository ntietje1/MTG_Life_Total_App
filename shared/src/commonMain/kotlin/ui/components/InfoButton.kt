package ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.question_icon
import org.jetbrains.compose.resources.vectorResource

@Composable
fun InfoButton(
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
) {
    SettingsButton(
        modifier = modifier,
        onPress = onPress,
        hapticEnabled = true,
        shadowEnabled = false,
        textSizeMultiplier = 0.9f,
        shape = RoundedCornerShape(100),
        imageVector = vectorResource(Res.drawable.question_icon),
        backgroundColor = MaterialTheme.colorScheme.onSurface,
        mainColor = MaterialTheme.colorScheme.onPrimary
    )
}