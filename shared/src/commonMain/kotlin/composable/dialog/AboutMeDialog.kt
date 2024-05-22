package composable.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Column {
            SettingsDialogHeader(
                modifier = Modifier.fillMaxWidth(),
                text = "APP DEVELOPMENT & DESIGN"
            )
            AboutMeDialogBody(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AboutMeDialogBody(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White.copy(alpha = 0.2f))
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            Modifier
                .wrapContentSize()
                .align(Alignment.Top)
                .padding(vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center),
                    bitmap = imageResource(Res.drawable.about_me),
                    contentDescription = null
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .padding(end = 5.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 5.dp),
                text = "Nick Tietje",
                style = TextStyle(
                    fontSize = 16.scaledSp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(
                modifier = Modifier.padding(bottom = 5.dp),
                text = "Nick is an undergraduate studying computer science and chemical engineering at Northeastern University and is pursuing a career in software development. He splits his time between reading, biking, and gaming.",
                style = TextStyle(
                    fontSize = 16.scaledSp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}