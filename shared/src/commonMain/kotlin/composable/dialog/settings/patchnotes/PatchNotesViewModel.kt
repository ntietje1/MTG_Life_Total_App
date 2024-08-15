package composable.dialog.settings.patchnotes

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class PatchNotesViewModel: ViewModel() {
    private val patchnotesUrl = "https://lcvgoezm16.execute-api.us-east-1.amazonaws.com/lifelinked/patchnotes"
    private val client = HttpClient()

    suspend fun getPatchNotes(): List<PatchNotesItem> {
        val response: HttpResponse = client.get(patchnotesUrl)
        val patchNotesResponse = Json.decodeFromString<PatchNotesResponse>(response.bodyAsText())
        return patchNotesResponse.patchNotes
    }
}
