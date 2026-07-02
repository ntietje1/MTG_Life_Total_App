package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.system.NotificationManager
import kotlinx.coroutines.delay
import theme.LocalDimensions
import theme.scaledSp

@Composable
fun BoxScope.InAppNotificationHost(
    notificationManager: NotificationManager,
    modifier: Modifier = Modifier
) {
    val notification by notificationManager.notification.collectAsState()
    val currentNotification = notification

    LaunchedEffect(currentNotification?.id) {
        val value = currentNotification ?: return@LaunchedEffect
        delay(value.durationMillis)
        notificationManager.dismiss(value)
    }

    AnimatedVisibility(
        modifier = modifier.align(Alignment.BottomCenter),
        visible = currentNotification != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        val dimensions = LocalDimensions.current
        Box(
            modifier = Modifier
                .padding(bottom = dimensions.paddingLarge * 2)
                .widthIn(max = 320.dp)
                .shadow(dimensions.borderMedium, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(8.dp))
                .semantics {
                    contentDescription = "Notification ${currentNotification?.message.orEmpty()}"
                }
                .padding(
                    horizontal = dimensions.paddingMedium,
                    vertical = dimensions.paddingSmall
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentNotification?.message.orEmpty(),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontSize = dimensions.textSmall.scaledSp,
                textAlign = TextAlign.Center
            )
        }
    }
}
