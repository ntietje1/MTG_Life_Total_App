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
