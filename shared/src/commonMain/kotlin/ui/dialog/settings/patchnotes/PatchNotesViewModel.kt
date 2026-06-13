package ui.dialog.settings.patchnotes

import androidx.lifecycle.ViewModel
import domain.storage.PreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class PatchNotesViewModel(
    private val patchNotesRepository: PatchNotesRepository,
    private val preferencesRepository: PreferencesRepository
): ViewModel() {
    private val _state = MutableStateFlow(PatchNotesState())
    val state: StateFlow<PatchNotesState> = _state.asStateFlow()

    private val patchnotesUrl = "https://lcvgoezm16.execute-api.us-east-1.amazonaws.com/lifelinked/patchnotes"
    private val client = HttpClient()

    suspend fun getPatchNotes(): Pair<List<PatchNotesItem>, List<String>>? {
        if (state.value.patchNotes.isEmpty()) {
            patchNotesRepository.load()?.let(::applyPatchNotes)
            try {
                val response: HttpResponse = client.get(patchnotesUrl)
                val patchNotesResponse = Json.decodeFromString<PatchNotesResponse>(response.bodyAsText())
                patchNotesRepository.save(patchNotesResponse)
                applyPatchNotes(patchNotesResponse)
            } catch (e: Exception) {
                if (state.value.patchNotes.isEmpty() && state.value.inProgress.isEmpty()) return null
            }
        }
        return Pair(state.value.patchNotes, state.value.inProgress)
    }

    private fun applyPatchNotes(response: PatchNotesResponse) {
        _state.value = _state.value.copy(
            patchNotes = response.patchNotes,
            inProgress = response.inProgress
        )
    }

    fun onSecretPatchNotesClick(): Boolean? {
        _state.value = _state.value.copy(secretPatchNotesClicks = state.value.secretPatchNotesClicks + 1)
        if (state.value.secretPatchNotesClicks > 0 && state.value.secretPatchNotesClicks % 5 == 0) {
            preferencesRepository.setDevMode(!preferencesRepository.devMode.value)
            return preferencesRepository.devMode.value
        }
        return null
    }
}
