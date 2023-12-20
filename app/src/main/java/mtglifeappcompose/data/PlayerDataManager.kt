package mtglifeappcompose.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlayerDataManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("player_data", Context.MODE_PRIVATE)

    init {
        with(sharedPreferences.edit()) {
            putString("[]", "error")
            apply()
        }
    }

    fun saveStartingLife(startingLife: Int) {
        with(sharedPreferences.edit()) {
            putInt("startingLife", startingLife)
            apply()
        }
        Player.startingLife = startingLife
    }

    fun loadStartingLife(): Int {
        return sharedPreferences.getInt("startingLife", 40)
    }

    fun savePlayer(player: Player, playerList: ArrayList<Player> = loadPlayers()) {
        deletePlayer(player, playerList)
        playerList.add(player)
        savePlayers(playerList)
    }

    fun deletePlayer(player: Player, playerList: ArrayList<Player> = loadPlayers()) {
        val iterator = playerList.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.name == player.name) {
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
