package ui.dialog.settings.patchnotes

import androidx.lifecycle.ViewModel
import data.SettingsManager
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class PatchNotesViewModel(
    private val settingsManager: SettingsManager
): ViewModel() {
    private val _state = MutableStateFlow(PatchNotesState())
    val state: StateFlow<PatchNotesState> = _state.asStateFlow()

    private val patchnotesUrl = "https://lcvgoezm16.execute-api.us-east-1.amazonaws.com/lifelinked/patchnotes"
    private val client = HttpClient()

    suspend fun getPatchNotes(): Pair<List<PatchNotesItem>, List<String>>? {
        if (state.value.patchNotes.isEmpty()) {
            try {
                val response: HttpResponse = client.get(patchnotesUrl)
                val patchNotesResponse = Json.decodeFromString<PatchNotesResponse>(response.bodyAsText())
                val patchNotes = patchNotesResponse.patchNotes
                val inProgress = patchNotesResponse.inProgress
                _state.value = _state.value.copy(patchNotes = patchNotes, inProgress = inProgress)
            } catch (e: Exception) {
                return null
            }
        }
        return Pair(state.value.patchNotes, state.value.inProgress)
    }

    fun onSecretPatchNotesClick(): Boolean? {
        _state.value = _state.value.copy(secretPatchNotesClicks = state.value.secretPatchNotesClicks + 1)
        if (state.value.secretPatchNotesClicks > 0 && state.value.secretPatchNotesClicks % 5 == 0) {
            settingsManager.setDevMode(!settingsManager.devMode.value)
            return settingsManager.devMode.value
        }
        return null
    }
}
