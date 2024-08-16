package composable.dialog.settings.patchnotes

import androidx.lifecycle.ViewModel
import composable.dialog.planechase.PlaneChaseState
import data.SettingsManager
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

class PatchNotesViewModel(
    private val settingsManager: SettingsManager
): ViewModel() {
    private val _state = MutableStateFlow(PatchNotesState())
    val state: StateFlow<PatchNotesState> = _state.asStateFlow()

    private val patchnotesUrl = "https://lcvgoezm16.execute-api.us-east-1.amazonaws.com/lifelinked/patchnotes"
    private val client = HttpClient()

    suspend fun getPatchNotes(): List<PatchNotesItem> {
        if (state.value.patchNotes.isEmpty()) {
            val response: HttpResponse = client.get(patchnotesUrl)
            val patchNotesResponse = Json.decodeFromString<PatchNotesResponse>(response.bodyAsText())
            val patchNotes = patchNotesResponse.patchNotes
            _state.value = _state.value.copy(patchNotes = patchNotes)
        }
        return state.value.patchNotes
    }

    fun onSecretPatchNotesClick(): Boolean? {
        _state.value = _state.value.copy(secretPatchNotesClicks = state.value.secretPatchNotesClicks + 1)
        if (state.value.secretPatchNotesClicks > 0 && state.value.secretPatchNotesClicks % 5 == 0) {
            settingsManager.devMode = !settingsManager.devMode
            return settingsManager.devMode
        }
        return null
    }
}
