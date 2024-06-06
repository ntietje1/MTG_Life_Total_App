package composable.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
        val iconSize = maxWidth / 5f
        val padding = maxWidth / 25f
        val textSize = (maxWidth / 24f).value.scaledSp

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
                            fontSize = textSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        modifier = Modifier.padding(bottom = padding),
                        text = "Nick is an undergraduate studying computer science and chemical engineering at Northeastern University and is pursuing a career in software development. He splits his time between reading, biking, and gaming.",
                        style = TextStyle(
                            fontSize = textSize,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AboutMeDialogBody(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(Modifier.wrapContentSize()) {
        val iconSize = maxHeight / 2f
        val padding = maxWidth / 25f
        val textSize = (maxWidth / 24f).value.scaledSp
        val buttonHeight = maxWidth / 7f

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .background(Color.White.copy(alpha = 0.2f))
                .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
                Box(Modifier.wrapContentSize().padding(horizontal = padding / 2f, vertical = padding / 4f)) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1.0f)
                            .clip(CircleShape),
                        bitmap = imageResource(Res.drawable.about_me),
                        contentDescription = null
                    )
                }
//                Box(
//                    modifier = Modifier
//                        .size(iconSize)
//                ) {
//                    Image(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .clip(CircleShape)
//                            .align(Alignment.Center),
//                        bitmap = imageResource(Res.drawable.about_me),
//                        contentDescription = null
//                    )
//                }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(end = padding)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = padding, top = padding),
                    text = "Nick Tietje",
                    style = TextStyle(
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    modifier = Modifier.padding(bottom = padding),
                    text = "Nick is an undergraduate studying computer science and chemical engineering at Northeastern University and is pursuing a career in software development. He splits his time between reading, biking, and gaming.",
                    style = TextStyle(
                        fontSize = textSize,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}