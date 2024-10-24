package data


import com.russhwolf.settings.Settings
import data.serializable.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsManager private constructor() {

    companion object {
        val instance: SettingsManager by lazy { SettingsManager() }
    }

    private val settings: Settings = Settings()

    var autoKo: Boolean
        get() = settings.getBoolean("autoKo", false)
        set(value) = settings.putBoolean("autoKo", value)

    var autoSkip: Boolean
        get() = settings.getBoolean("autoSkip", false)
        set(value) = settings.putBoolean("autoSkip", value)

    var keepScreenOn: Boolean
        get() = settings.getBoolean("keepScreenOn", false)
        set(value) = settings.putBoolean("keepScreenOn", value)

    var cameraRollDisabled: Boolean
        get() = settings.getBoolean("cameraRollDisabled", false)
        set(value) = settings.putBoolean("cameraRollDisabled", value)

    var fastCoinFlip: Boolean
        get() = settings.getBoolean("fastCoinFlip", false)
        set(value) = settings.putBoolean("fastCoinFlip", value)

    var numPlayers: Int
        get() = settings.getInt("numPlayers", 4)
        set(value) = settings.putInt("numPlayers", value).apply {
            println("numPlayers: $value")
        }

    var alt4PlayerLayout: Boolean
        get() = settings.getBoolean("alt4PlayerLayout", false)
        set(value) = settings.putBoolean("alt4PlayerLayout", value)

    var darkTheme: Boolean
        get() = settings.getBoolean("darkTheme", true)
        set(value) = settings.putBoolean("darkTheme", value)

    var startingLife: Int
        get() = settings.getInt("startingLife", 40)
        set(value) = settings.putInt("startingLife", value)

    var tutorialSkip: Boolean
        get() = settings.getBoolean("tutorialSkip", false)
        set(value) = settings.putBoolean("tutorialSkip", value)

//    var turnTimer: Boolean
//        get() = settings.getBoolean("turnTimer", false)
//        set(value) = settings.putBoolean("turnTimer", value)

    private val _turnTimer = MutableStateFlow(settings.getBoolean("turnTimer", false))
    val turnTimer = _turnTimer.asStateFlow()

    fun setTurnTimer(value: Boolean) {
        settings.putBoolean("turnTimer", value)
        _turnTimer.value = value
    }

    var devMode: Boolean
        get() = settings.getBoolean("devMode", false)
        set(value) = settings.putBoolean("devMode", value)

    var catGifButton: Boolean
        get() = settings.getBoolean("catGifButton", false)
        set(value) = settings.putBoolean("catGifButton", value)

    fun loadPlayerStates(): List<Player> {
        val allPrefString = settings.getString("playerStates", "[]")
        return Json.decodeFromString<List<Player>>(allPrefString)
    }

    fun savePlayerStates(players: List<Player>) {
        val allPrefString = Json.encodeToString(players)
        settings.putString("playerStates", allPrefString)
    }

    fun savePlanechaseState(planarDeck: List<Card>, planarBackStack: List<Card>) {
        val allPrefString = Json.encodeToString(planarDeck)
        settings.putString("planarDeck", allPrefString)
        val backPrefString = Json.encodeToString(planarBackStack)
        settings.putString("planarBackStack", backPrefString)
    }

    fun loadPlanechaseState(): Pair<List<Card>, List<Card>> {
        val deckPrefString = settings.getString("planarDeck", "[]")
        val backPrefString = settings.getString("planarBackStack", "[]")
        return Pair(Json.decodeFromString(deckPrefString), Json.decodeFromString(backPrefString))
    }

    fun savePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        removePlayerPref(player, playerList)
        playerList.add(player)
        savePlayerPrefs(playerList)
    }

    private fun removePlayerPref(player: Player, playerList: ArrayList<Player>) {
        val iterator = playerList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == player.name) {
                iterator.remove()
            }
        }
    }

    fun deletePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        removePlayerPref(player, playerList)
        savePlayerPrefs(playerList)
    }

//    fun loadPlayerPref(player: Player): Player? {
//        val playerList = loadPlayerPrefs()
//        return playerList.find { it.name == player.name }
//    }

    fun loadPlayerPrefs(): ArrayList<Player> {
        val allPrefString = settings.getString("playerPrefs", "[]")
        return Json.decodeFromString<List<Player>>(allPrefString).reversed().toCollection(ArrayList())
    }

    private fun savePlayerPrefs(players: ArrayList<Player>) {
        val allPrefString = Json.encodeToString(players)
        settings.putString("playerPrefs", allPrefString)
    }
}
