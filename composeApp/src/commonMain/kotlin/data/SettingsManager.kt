package data


import com.russhwolf.settings.Settings
import data.serializable.Card
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal

/**
 * A singleton object that manages the application's settings.
 */
@ThreadLocal
object SettingsManager {
    /**
     * The [Settings] object that is used to store the settings.
     */
    private val settings: Settings = Settings()

    /**
     * Setting for whether or not to automatically knock out players at 0 life.
     */
    var autoKo: Boolean
        get() = settings.getBoolean("autoKo", false)
        set(value) = settings.putBoolean("autoKo", value)

    /**
     * Setting for whether or not to automatically skip the player select screen.
     */
    var autoSkip: Boolean
        get() = settings.getBoolean("autoSkip", false)
        set(value) = settings.putBoolean("autoSkip", value)

    /**
     * Setting to keep the screen on while the app is open.
     */
    var keepScreenOn: Boolean
        get() = settings.getBoolean("keepScreenOn", false)
        set(value) = settings.putBoolean("keepScreenOn", value)

    /**
     * Setting for whether or not allow camera roll access in-game
     */
    var cameraRollDisabled: Boolean
        get() = settings.getBoolean("cameraRollDisabled", false)
        set(value) = settings.putBoolean("cameraRollDisabled", value)

    /**
     * Setting for speeding up the coin flip animation
     */
    var fastCoinFlip: Boolean
        get() = settings.getBoolean("fastCoinFlip", false)
        set(value) = settings.putBoolean("fastCoinFlip", value)

    /**
     * Setting for the number of active players
     */
    var numPlayers: Int
        get() = settings.getInt("numPlayers", 4)
        set(value) = settings.putInt("numPlayers", value)

    /**
     * Setting for whether or not to use the alt 4 player layout
     */
    var alt4PlayerLayout: Boolean
        get() = settings.getBoolean("alt4PlayerLayout", false)
        set(value) = settings.putBoolean("alt4PlayerLayout", value)

    /**
     * Setting for light/dark theme
     */
    var darkTheme: Boolean
        get() = settings.getBoolean("darkTheme", true)
        set(value) = settings.putBoolean("darkTheme", value)

    /**
     * Setting for starting life total
     */
    var startingLife: Int
        get() = settings.getInt("startingLife", 40)
        set(value) = settings.putInt("startingLife", value)

    /**
     * Load the player states from the storage
     */
    fun loadPlayerStates(): List<Player> {
        val allPrefString = settings.getString("playerStates", "[]")
        return Json.decodeFromString(allPrefString)
    }

    /**
     * Save the player states to the storage
     */
    fun savePlayerStates(players: List<Player>) {
            val allPrefString = Json.encodeToString(players)
            settings.putString("playerStates", allPrefString)
    }

    /**
     * Load the list of all planes
     */
    fun loadAllPlanes(): List<Card> {
        val allPrefString = settings.getString("allPlanes", "[]")
        return Json.decodeFromString(allPrefString)
    }

    /**
     * Save the list of all planes
     */
    fun saveAllPlanes(planes: List<Card>) {
        val allPrefString = Json.encodeToString(planes)
        settings.putString("allPlanes", allPrefString)
    }

    /**
     * Save the planar deck
     */
    fun savePlanarDeck(deck: List<Card>) {
        val allPrefString = Json.encodeToString(deck)
        settings.putString("planarDeck", allPrefString)
    }

    /**
     * Load the planar deck
     */
    fun loadPlanarDeck(): List<Card> {
        val allPrefString = settings.getString("planarDeck", "[]")
        return Json.decodeFromString(allPrefString)
    }

    /**
     * Save a player's customizations
     */
    fun savePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        if (player.isDefaultName()) return
        deletePlayerPref(player, playerList)
        playerList.add(player)
        savePlayerPrefs(playerList)
    }

    /**
     * Delete a player's customizations
     */
    fun deletePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        val iterator = playerList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == player.name) {
                iterator.remove()
            }
        }
        savePlayerPrefs(playerList)
    }

    /**
     * Load all player customizations
     */
    fun loadPlayerPrefs(): ArrayList<Player> {
        val allPrefString = settings.getString("playerPrefs", "[]")
        return Json.decodeFromString(allPrefString)
    }

    /**
     * Save all player customizations
     */
    private fun savePlayerPrefs(players: ArrayList<Player>) {
        val allPrefString = Json.encodeToString(players)
        settings.putString("playerPrefs", allPrefString)
    }
}
