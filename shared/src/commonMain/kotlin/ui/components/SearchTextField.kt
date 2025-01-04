package ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.search_icon
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions


@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    label: String = "Search Scryfall",
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    searchInProgress: Boolean = false,
    onSearch: () -> Unit
) {
    val dimensions = LocalDimensions.current
    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxHeight / 100f }
        TextFieldWithButton(
            modifier = modifier,
            value = query,
            onValueChange = onQueryChange,
            label = label,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
            ), keyboardActions = KeyboardActions(onSearch = {
                onSearch()
            })
        ) {
            IconButton(
                onClick = {
                    onSearch()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (searchInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding * 1.5f),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderMedium
                    )
                } else {
                    Icon(
                        imageVector = vectorResource(Res.drawable.search_icon),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }
        }
    }
}