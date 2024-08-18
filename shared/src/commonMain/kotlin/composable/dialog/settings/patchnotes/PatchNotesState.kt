package composable.dialog.settings.patchnotes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PatchNotesState(
    val patchNotes: List<PatchNotesItem> = emptyList(),
    val secretPatchNotesClicks: Int = 0
)

@Serializable
data class PatchNotesItem(
    val version: String,
    val title: String,
    val date: String,
    val notes: List<String>
)

@Serializable
data class PatchNotesResponse(
    @SerialName("patch-notes")
    val patchNotes: List<PatchNotesItem>
)