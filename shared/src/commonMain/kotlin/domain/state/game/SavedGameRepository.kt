package domain.state.game

import com.russhwolf.settings.Settings
import domain.state.legacy.LocalGameSessionMapper
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.storage.PreferencesRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Player

class SavedGameRepository(
    private val settings: Settings,
    private val preferencesRepository: PreferencesRepository,
    private val sessionId: GameSessionId = GameSessionId("local-active-game")
) : GameSessionRepository {
    override suspend fun loadActiveSession(): GameSession {
        val savedJson = settings.getStringOrNull(ActiveGameKey)
        if (savedJson != null) {
            return loadSavedGame(savedJson)
        }

        val session = LocalGameSessionMapper.fromLegacyOrFresh(
            id = sessionId,
            rules = GameRules(startingLife = preferencesRepository.startingLife.value),
            legacyPlayers = loadLegacyPlayerStates(),
            fallbackSeatCount = preferencesRepository.numPlayers.value
        )
        saveSession(session)
        return session
    }

    override suspend fun commit(mutation: GameMutation): CommitResult {
        return try {
            saveSession(mutation.resultingSession)
            CommitResult.Success
        } catch (error: Exception) {
            CommitResult.Failure(error.message ?: "Failed to save game session")
        }
    }

    private fun loadSavedGame(savedJson: String): GameSession {
        return try {
            JsonFormat.decodeFromString<SavedGameDto>(savedJson).toDomain()
        } catch (error: Exception) {
            settings.putString(CorruptActiveGameKey, savedJson)
            settings.remove(ActiveGameKey)
            freshSession()
        }
    }

    private fun saveSession(session: GameSession) {
        settings.putString(ActiveGameKey, JsonFormat.encodeToString(SavedGameDto.fromDomain(session)))
    }

    private fun loadLegacyPlayerStates(): List<Player> {
        val savedJson = settings.getStringOrNull(LegacyPlayerStatesKey) ?: return emptyList()
        return try {
            JsonFormat.decodeFromString(savedJson)
        } catch (error: Exception) {
            settings.putString(CorruptLegacyPlayerStatesKey, savedJson)
            settings.remove(LegacyPlayerStatesKey)
            emptyList()
        }
    }

    private fun freshSession(): GameSession {
        return GameSession.newGame(
            id = sessionId,
            rules = GameRules(startingLife = preferencesRepository.startingLife.value),
            appearances = (1..preferencesRepository.numPlayers.value.coerceIn(1, GameSession.MaxSeats)).map { seatNumber ->
                SeatAppearance(displayName = "P$seatNumber")
            }
        )
    }

    companion object {
        const val ActiveGameKey = "savedGame"
        const val CorruptActiveGameKey = "savedGame.corrupt"
        const val LegacyPlayerStatesKey = "playerStates"
        const val CorruptLegacyPlayerStatesKey = "playerStates.corrupt"

        private val JsonFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

@Serializable
private data class SavedGameDto(
    val version: Int = CurrentVersion,
    val id: String,
    val rules: GameRulesDto = GameRulesDto(),
    val seats: List<GameSeatDto>,
    val commanderDamage: List<CommanderDamageDto> = emptyList(),
    val commanderMode: CommanderModeDto? = null,
    val tableCounters: Map<String, Int> = emptyMap(),
    val monarchSeatId: String? = null,
    val dayNight: String = DayNight.NONE.name,
    val sessionVersion: Long = 0
) {
    fun toDomain(): GameSession {
        return GameSession(
            id = GameSessionId(id),
            rules = rules.toDomain(),
            seats = seats.map { it.toDomain() },
            commander = commanderDamage.fold(CommanderDamageMatrix()) { matrix, damage ->
                damage.applyTo(matrix)
            },
            commanderMode = commanderMode?.toDomain(),
            tableCounters = tableCounters.mapNotNull { (key, value) ->
                enumValueOrNull<TableCounterType>(key)?.let { it to value }
            }.toMap(),
            monarchSeatId = monarchSeatId?.let(::SeatId),
            dayNight = enumValueOrNull<DayNight>(dayNight) ?: DayNight.NONE,
            version = sessionVersion
        )
    }

    companion object {
        private const val CurrentVersion = 1

        fun fromDomain(session: GameSession): SavedGameDto {
            return SavedGameDto(
                id = session.id.value,
                rules = GameRulesDto.fromDomain(session.rules),
                seats = session.seats.map(GameSeatDto::fromDomain),
                commanderDamage = session.commander.entries().map { (key, value) ->
                    CommanderDamageDto.fromDomain(key, value)
                },
                commanderMode = session.commanderMode?.let(CommanderModeDto::fromDomain),
                tableCounters = session.tableCounters.mapKeys { (counter, _) -> counter.name },
                monarchSeatId = session.monarchSeatId?.value,
                dayNight = session.dayNight.name,
                sessionVersion = session.version
            )
        }
    }
}

@Serializable
private data class GameRulesDto(
    val startingLife: Int = 40
) {
    fun toDomain(): GameRules {
        return GameRules(startingLife = startingLife)
    }

    companion object {
        fun fromDomain(rules: GameRules): GameRulesDto {
            return GameRulesDto(startingLife = rules.startingLife)
        }
    }
}

@Serializable
private data class GameSeatDto(
    val id: String,
    val displayName: String,
    val backgroundArgb: Int = PlayerColors().backgroundArgb,
    val textArgb: Int = PlayerColors().textArgb,
    val background: PlayerBackgroundDto = PlayerBackgroundDto.None,
    val sourceProfileId: String? = null,
    val lifeValue: Int,
    val lifeRecentChange: Int = 0,
    val manualDeath: Boolean = false,
    val counters: Map<String, Int> = emptyMap(),
    val activeCounters: List<String> = emptyList()
) {
    fun toDomain(): GameSeat {
        return GameSeat(
            id = SeatId(id),
            appearance = SeatAppearance(
                displayName = displayName,
                colors = PlayerColors(
                    backgroundArgb = backgroundArgb,
                    textArgb = textArgb
                ),
                background = background.toDomain(),
                sourceProfileId = sourceProfileId?.let(::PlayerProfileId)
            ),
            life = TrackedInt(
                value = lifeValue,
                recentChange = lifeRecentChange
            ),
            manualDeath = manualDeath,
            counters = counters.mapNotNull { (key, value) ->
                enumValueOrNull<CounterType>(key)?.let { it to value }
            }.toMap(),
            activeCounters = activeCounters.mapNotNullTo(mutableSetOf()) { key ->
                enumValueOrNull<CounterType>(key)
            }
        )
    }

    companion object {
        fun fromDomain(seat: GameSeat): GameSeatDto {
            return GameSeatDto(
                id = seat.id.value,
                displayName = seat.appearance.displayName,
                backgroundArgb = seat.appearance.colors.backgroundArgb,
                textArgb = seat.appearance.colors.textArgb,
                background = PlayerBackgroundDto.fromDomain(seat.appearance.background),
                sourceProfileId = seat.appearance.sourceProfileId?.value,
                lifeValue = seat.life.value,
                lifeRecentChange = seat.life.recentChange,
                manualDeath = seat.manualDeath,
                counters = seat.counters.mapKeys { (counter, _) -> counter.name },
                activeCounters = seat.activeCounters.map { it.name }
            )
        }
    }
}

@Serializable
private sealed interface PlayerBackgroundDto {
    fun toDomain(): PlayerBackground

    @Serializable
    @SerialName("none")
    data object None : PlayerBackgroundDto {
        override fun toDomain(): PlayerBackground = PlayerBackground.None
    }

    @Serializable
    @SerialName("localImage")
    data class LocalImage(val fileName: String) : PlayerBackgroundDto {
        override fun toDomain(): PlayerBackground = PlayerBackground.LocalImage(fileName)
    }

    @Serializable
    @SerialName("providerImage")
    data class ProviderImage(val url: String) : PlayerBackgroundDto {
        override fun toDomain(): PlayerBackground = PlayerBackground.ProviderImage(url)
    }

    @Serializable
    @SerialName("cardArt")
    data class CardArt(val url: String) : PlayerBackgroundDto {
        override fun toDomain(): PlayerBackground = PlayerBackground.CardArt(url)
    }

    companion object {
        fun fromDomain(background: PlayerBackground): PlayerBackgroundDto {
            return when (background) {
                PlayerBackground.None -> None
                is PlayerBackground.LocalImage -> LocalImage(background.fileName)
                is PlayerBackground.ProviderImage -> ProviderImage(background.url)
                is PlayerBackground.CardArt -> CardArt(background.url)
            }
        }
    }
}

@Serializable
private data class CommanderDamageDto(
    val dealerSeatId: String,
    val receiverSeatId: String,
    val partner: Boolean,
    val value: Int,
    val recentChange: Int = 0
) {
    fun applyTo(matrix: CommanderDamageMatrix): CommanderDamageMatrix {
        return CommanderDamageMatrix(
            matrix.entries() + (
                CommanderDamageKey(
                    dealerSeatId = SeatId(dealerSeatId),
                    receiverSeatId = SeatId(receiverSeatId),
                    partner = partner
                ) to TrackedInt(value = value, recentChange = recentChange)
            )
        )
    }

    companion object {
        fun fromDomain(key: CommanderDamageKey, value: TrackedInt): CommanderDamageDto {
            return CommanderDamageDto(
                dealerSeatId = key.dealerSeatId.value,
                receiverSeatId = key.receiverSeatId.value,
                partner = key.partner,
                value = value.value,
                recentChange = value.recentChange
            )
        }
    }
}

@Serializable
private data class CommanderModeDto(
    val dealerSeatId: String,
    val partnerMode: Boolean = false
) {
    fun toDomain(): CommanderMode {
        return CommanderMode(
            dealerSeatId = SeatId(dealerSeatId),
            partnerMode = partnerMode
        )
    }

    companion object {
        fun fromDomain(mode: CommanderMode): CommanderModeDto {
            return CommanderModeDto(
                dealerSeatId = mode.dealerSeatId.value,
                partnerMode = mode.partnerMode
            )
        }
    }
}

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? {
    return enumValues<T>().firstOrNull { it.name == name }
}
