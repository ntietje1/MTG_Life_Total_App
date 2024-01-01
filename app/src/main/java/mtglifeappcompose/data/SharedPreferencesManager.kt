package mtglifeappcompose.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SharedPreferencesManager {

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("[]", "error")
            apply()
        }
    }

    fun loadAllPlanes(): List<Card> {
        val allPrefString = sharedPreferences.getString("allPlanes", "[]")!!
        return Json.decodeFromString(allPrefString)
    }

    fun saveAllPlanes(planes: List<Card>) {
        val allPrefString = Json.encodeToString(planes)
        with(sharedPreferences.edit()) {
            putString("allPlanes", allPrefString)
            apply()
        }
    }

    fun savePlanarDeck(deck: List<Card>) {
        val allPrefString = Json.encodeToString(deck)
        with(sharedPreferences.edit()) {
            putString("planarDeck", allPrefString)
            apply()
        }
    }

    fun loadPlanarDeck(): List<Card> {
        val allPrefString = sharedPreferences.getString("planarDeck", "[]")!!
        return Json.decodeFromString(allPrefString)
    }

    fun loadDisableBackButton(): Boolean {
        return sharedPreferences.getBoolean("disableBackButton", false)
    }

    fun saveDisableBackButton(disableBackButton: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("disableBackButton", disableBackButton)
            apply()
        }
    }

    fun saveAutoSkip(autoSkip: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("autoSkip", autoSkip)
            apply()
        }
    }

    fun loadAutoSkip(): Boolean {
        return sharedPreferences.getBoolean("autoSkip", false)
    }

    fun saveKeepScreenOn(keepScreenOn: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("keepScreenOn", keepScreenOn)
            apply()
        }
    }

    fun loadKeepScreenOn(): Boolean {
        return sharedPreferences.getBoolean("keepScreenOn", false)
    }

    fun saveAutoKo(autoKo: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("autoKo", autoKo)
            apply()
        }
    }

    fun loadAutoKo(): Boolean {
        return sharedPreferences.getBoolean("autoKo", false)
    }

    fun saveRotatingMiddleButton(rotatingMiddleButton: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("rotatingMiddleButton", rotatingMiddleButton)
            apply()
        }
    }

    fun loadRotatingMiddleButton(): Boolean {
        return sharedPreferences.getBoolean("rotatingMiddleButton", true)
    }

    fun saveCameraRollDisabled(cameraRollDisabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("cameraRollEnabled", cameraRollDisabled)
            apply()
        }
    }

    fun loadCameraRollDisabled(): Boolean {
        return sharedPreferences.getBoolean("cameraRollDisabled", false)
    }

    fun saveFastCoinFlip(fastCoinFlip: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("fastCoinFlip", fastCoinFlip)
            apply()
        }
    }

    fun loadFastCoinFlip(): Boolean {
        return sharedPreferences.getBoolean("fastCoinFlip", false)
    }

    fun loadNumPlayers(): Int {
        return sharedPreferences.getInt("numPlayers", 4)
    }

    fun saveNumPlayers(numPlayers: Int) {
        with(sharedPreferences.edit()) {
            putInt("numPlayers", numPlayers)
            apply()
        }
    }

    fun save4PlayerLayout(alt4PlayerLayout: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("alt4PlayerLayout", alt4PlayerLayout)
            apply()
        }
    }

    fun load4PlayerLayout(): Boolean {
        return sharedPreferences.getBoolean("alt4PlayerLayout", false)
    }

    fun saveTheme(darkTheme: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("darkTheme", darkTheme)
            apply()
        }
    }

    fun loadTheme(): Boolean {
        return sharedPreferences.getBoolean("darkTheme", true)
    }

    fun saveStartingLife(startingLife: Int) {
        with(sharedPreferences.edit()) {
            putInt("startingLife", startingLife)
            apply()
        }
    }

    fun loadStartingLife(): Int {
        return sharedPreferences.getInt("startingLife", 40)
    }

    fun savePlayer(player: Player, playerList: ArrayList<Player> = loadPlayers()) {
        if (player.isDefaultName()) return
        deletePlayer(player, playerList)
        playerList.add(player)
        savePlayers(playerList)
    }

    fun deletePlayer(player: Player, playerList: ArrayList<Player> = loadPlayers()) {
        val iterator = playerList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == player.name) {
                iterator.remove()
            }
        }
        savePlayers(playerList)
    }

    fun loadPlayers(): ArrayList<Player> {
        val allPrefString = sharedPreferences.getString("playerPrefs", "[]")!!
        return Json.decodeFromString(allPrefString)
    }

    private fun savePlayers(players: ArrayList<Player>) {
        val allPrefString = Json.encodeToString(players)
        with(sharedPreferences.edit()) {
            putString("playerPrefs", allPrefString)
            apply()
        }
    }
}
