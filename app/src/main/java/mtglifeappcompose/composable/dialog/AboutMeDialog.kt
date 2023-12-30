package mtglifeappcompose.composable.dialog

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Preview
@Composable
fun AboutMeDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit = {}
) {
    BoxWithConstraints(modifier) {
        Column() {
            AboutMeDialogHeader(
                modifier = Modifier.fillMaxWidth(),
                text = "APP DEVELOPMENT & DESIGN"
            )
            AboutMeDialogBody(modifier = Modifier.fillMaxWidth())
        }

    }
}

@Composable
fun AboutMeDialogHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    SettingsDialogHeader(
        text = text,
        modifier = modifier
    )
}

@Composable
fun AboutMeDialogBody(
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(id = R.drawable.placeholder_icon),
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
            SettingsButton(
                modifier = Modifier
                    .padding(horizontal = 7.5.dp)
                    .size(60.dp),
                shadowEnabled = false,
                imageResource = icon,
                mainColor = MaterialTheme.colorScheme.onPrimary,
                enabled = false
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 5.dp),
                text = "Nick Tietje",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(
                modifier = Modifier.padding(bottom = 5.dp),
                text = "Nick is an undergraduate studying computer science and chemical engineering at Northeastern University. He splits his time between reading, app development, and biking.",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}