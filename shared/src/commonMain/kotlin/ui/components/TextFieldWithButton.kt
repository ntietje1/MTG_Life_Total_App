package ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import theme.halfAlpha
import theme.scaledSp

@Composable
fun TextFieldWithButton(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    button: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val textSize = remember(Unit) { (maxHeight / 3.75f).value }
        TextField(
            modifier = Modifier.fillMaxHeight().width(maxWidth - maxHeight).padding(top = textSize.dp / 32f, bottom = textSize.dp / 64f).align(Alignment.CenterStart),
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    modifier = Modifier.wrapContentSize().padding(bottom = textSize.dp / 8f),
                    text = label,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontSize = (textSize * 0.8f).scaledSp
                )
            },
            textStyle = TextStyle(fontSize = (textSize * 1.3f).scaledSp),
            singleLine = true, colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.onSurface.halfAlpha(),
                ),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ), keyboardOptions = keyboardOptions, keyboardActions = keyboardActions
        )
        BoxWithConstraints(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .aspectRatio(1.0f)
        ) {
            button()
        }
    }
}