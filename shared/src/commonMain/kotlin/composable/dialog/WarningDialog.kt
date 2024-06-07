package composable.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import theme.scaledSp

@Composable
fun WarningDialog(
    title: String,
    message: String,
    optionOneEnabled: Boolean = true,
    optionTwoEnabled: Boolean = true,
    optionOneMessage: String = "Confirm",
    optionTwoMessage: String = "Dismiss",
    onOptionOne: () -> Unit = {},
    onOptionTwo: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
        Dialog(
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = true, usePlatformDefaultWidth = false),
            onDismissRequest = {
                onDismiss()
            }
        ) {
            BoxWithConstraints(Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(15))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 1.0f), RoundedCornerShape(15))
                .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f), RoundedCornerShape(15))
            ) {
                val textSize = (maxWidth / 25f).value.scaledSp
                val padding = maxWidth / 45f
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(padding, Alignment.CenterVertically)
                ) {
                    Text(
                        text = title,
                        fontSize = textSize * 1.1f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(start = padding*2, top = padding*2, end = padding*2)
                    )
                    Text(
                        text = message,
                        fontSize = textSize*0.9f,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = padding*2)
                    )
                    Spacer(modifier = Modifier.height(padding*2))
                    Row(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (optionTwoEnabled) {
                            TextButton(onClick = {
                                onDismiss()
                                onOptionTwo()
                            }, modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                Text(
                                    text = optionTwoMessage,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = textSize,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = padding*2)
                                )
                            }
                        }
                        if (optionOneEnabled) {
                            TextButton(onClick = {
                                onDismiss()
                                onOptionOne()
                            }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = optionOneMessage,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = textSize,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = padding*2)
                                )
                            }
                        }
                    }
                }
            }

//        AlertDialog(
//            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = true, usePlatformDefaultWidth = false),
//            onDismissRequest = {
//                onDismiss()
//            },
//            title = {
//                Text(
//                    text = title,
//                    fontSize = textSize
//                )
//            },
//            text = {
//                Text(
//                    modifier = Modifier.width(width),
//                    text = message,
//                    fontSize = textSize * 0.9f
//                )
//            },
//            confirmButton = {
//                if (optionOneEnabled) {
//                    TextButton(onClick = {
//                        onDismiss()
//                        onOptionOne()
//                    }) {
//                        Text(
//                            text = optionOneMessage,
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            fontSize = textSize
//                        )
//                    }
//                }
//            },
//            dismissButton = {
//                if (optionTwoEnabled) {
//                    TextButton(onClick = {
//                        onDismiss()
//                        onOptionTwo()
//                    }) {
//                        Text(
//                            text = optionTwoMessage,
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            fontSize = textSize
//                        )
//                    }
//                }
//            }
//        )
    }
}