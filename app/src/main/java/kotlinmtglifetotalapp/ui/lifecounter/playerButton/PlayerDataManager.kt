package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.SharedPreferences

class PlayerDataManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("player_data", Context.MODE_PRIVATE)

    init {
        with(sharedPreferences.edit()) {
            putString("default", "error")
            apply()
        }
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

        val res = arrayListOf<Player>()
        val allPrefString = sharedPreferences.getString("playerPrefs", "default")!!
        val pstrings = allPrefString.split(",")
        if (pstrings.isEmpty() || pstrings[0] == "default") {
            return arrayListOf()
        }
        for (p in pstrings) {
            val player = Player(40)
            player.fromString(p)
            res.add(player)
        }
        return res
    }

    fun savePlayers(players: ArrayList<Player>) {
        val allPrefString = Player.allToString(players)
        with(sharedPreferences.edit()) {
            putString("playerPrefs", allPrefString)
            apply()
        }
    }
}
