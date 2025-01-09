package ui.dialog.settings.patchnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import domain.system.NotificationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.halfAlpha
import theme.scaledSp
import ui.dialog.settings.SettingsDialogHeader


@Composable
fun PatchNotesDialogContent(
    modifier: Modifier = Modifier,
    viewModel: PatchNotesViewModel = koinInject(),
    notificationManager: NotificationManager = koinInject()
) {
    val patchNotes = remember { mutableStateListOf<PatchNotesItem>() }
    val inProgressItems = remember { mutableStateListOf<String>() }
    val dimensions = LocalDimensions.current

    viewModel.viewModelScope.launch {
        viewModel.getPatchNotes()?.let { (patchNotesItems, inProgress) ->
            patchNotes.addAll(patchNotesItems)
            inProgressItems.addAll(inProgress)
        }
    }

    BoxWithConstraints(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                SettingsDialogHeader(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Change Log",
                )
            }
            if (patchNotes.isNotEmpty() || inProgressItems.isNotEmpty()) {
                item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = dimensions.borderThin,
                        color = MaterialTheme.colorScheme.onPrimary.halfAlpha(),
                    )
                }
            }
            if (inProgressItems.isNotEmpty()) {
                item {
                    InProgressItems(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        items = inProgressItems
                    )
                }
            }
            if (inProgressItems.isNotEmpty() && patchNotes.isNotEmpty()) {
                item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = dimensions.borderThin,
                        color = MaterialTheme.colorScheme.onPrimary.halfAlpha(),
                    )
                }
            }
            if (patchNotes.isNotEmpty()) {
                itemsIndexed(patchNotes) { index, it ->
                    PatchNotesItem(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().then(
                            if (patchNotes.indexOf(it) == patchNotes.size - 1) Modifier.clickable {
                                val result = viewModel.onSecretPatchNotesClick()
                                if (result != null && result) {
                                    notificationManager.showNotification("Secret Mode Enabled")
                                } else if (result != null && !result) {
                                    notificationManager.showNotification("Secret Mode Disabled")
                                }
                            } else Modifier
                        ),
                        version = it.version,
                        title = it.title,
                        date = it.date,
                        notes = it.notes
                    )
                    if (index != 0 && index != patchNotes.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = dimensions.borderThin,
                            color = MaterialTheme.colorScheme.onPrimary.halfAlpha(),
                        )
                    }
                }
            }
            if (patchNotes.isNotEmpty() || inProgressItems.isNotEmpty()) {
                item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = dimensions.borderThin,
                        color = MaterialTheme.colorScheme.onPrimary.halfAlpha(),
                    )
                }
            }
        }
    }
}

@Composable
fun InProgressItems(
    modifier: Modifier = Modifier,
    items: List<String>
) {
    BoxWithConstraints(modifier) {
        val padding = remember(Unit) { maxWidth / 25f }
        val textSize = remember(Unit) { (maxWidth / 24f).value }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.onSurface.halfAlpha()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = padding, start = padding / 1.25f)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = padding / 7f, top = padding / 2f),
                    text = "In Development",
                    style = TextStyle(
                        fontSize = textSize.scaledSp * 1.2f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.padding(bottom = padding / 2f, top = padding / 7f))
                items.forEach {
                    Text(
                        modifier = Modifier.padding(bottom = padding / 1.5f),
                        text = "• $it",
                        style = TextStyle(
                            fontSize = textSize.scaledSp * 0.9f,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PatchNotesItem(
    modifier: Modifier = Modifier,
    title: String,
    version: String,
    date: String,
    notes: List<String>
) {
    BoxWithConstraints(modifier) {
        val padding = remember(Unit) { maxWidth / 25f }
        val textSize = remember(Unit) { (maxWidth / 24f).value }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.onSurface.halfAlpha()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = padding, start = padding / 1.25f)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = padding / 7f, top = padding / 2f),
                    text = "$version - $title",
                    style = TextStyle(
                        fontSize = textSize.scaledSp * 1.2f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    modifier = Modifier.padding(bottom = padding / 2f, top = padding / 7f, start = padding / 10f),
                    text = date,
                    style = TextStyle(
                        fontSize = textSize.scaledSp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                notes.forEach {
                    Text(
                        modifier = Modifier.padding(bottom = padding / 1.5f),
                        text = "• $it",
                        style = TextStyle(
                            fontSize = textSize.scaledSp * 0.9f,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}
