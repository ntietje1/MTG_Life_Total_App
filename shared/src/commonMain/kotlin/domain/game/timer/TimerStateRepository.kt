package domain.game.timer

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimerStateRepository(
    private val settings: Settings
) {
    fun load(): GameTimerState? {
        val savedJson = settings.getStringOrNull(StateKey) ?: return null
        return try {
            JsonFormat.decodeFromString<GameTimerState>(savedJson)
        } catch (error: Exception) {
            settings.putString(CorruptStateKey, savedJson)
            settings.remove(StateKey)
            null
        }
    }

    fun save(state: GameTimerState?) {
        if (state == null) {
            settings.remove(StateKey)
        } else {
            settings.putString(StateKey, JsonFormat.encodeToString(state))
        }
    }

    companion object {
        const val StateKey = "savedTimerState"
        const val CorruptStateKey = "savedTimerState.corrupt"

        private val JsonFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
