package ui.dialog.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.about_me
import org.jetbrains.compose.resources.imageResource
import theme.scaledSp

@Composable
fun AboutMeDialogContent(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val iconSize = remember(Unit) { maxWidth / 5f }
        val padding = remember(Unit) {  maxWidth / 25f }
        val textSize = remember(Unit) {  (maxWidth / 24f).value }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            SettingsDialogHeader(
                modifier = Modifier.fillMaxWidth(),
                text = "App Development & Design",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                    Image(
                        modifier = Modifier
                            .size(iconSize)
                            .padding(horizontal = padding / 2f, vertical = padding / 4f)
                            .aspectRatio(1.0f)
                            .clip(CircleShape),
                        bitmap = imageResource(Res.drawable.about_me),
                        contentDescription = null
                    )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(end = padding)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = padding, top = padding),
                        text = "Nick Tietje",
                        style = TextStyle(
                            fontSize = textSize.scaledSp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        modifier = Modifier.padding(bottom = padding),
                        text = "Nick is an undergraduate studying computer science and chemical engineering at Northeastern University and is pursuing a career in software development. He splits his time between reading, biking, and gaming.",
                        style = TextStyle(
                            fontSize = textSize.scaledSp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}