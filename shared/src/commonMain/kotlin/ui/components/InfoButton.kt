package ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
        textSizeMultiplier = 0.9f,
        shape = RoundedCornerShape(100),
        imageVector = vectorResource(Res.drawable.question_icon),
        backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
        mainColor = MaterialTheme.colorScheme.onPrimary
    )
}