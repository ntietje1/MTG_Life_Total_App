package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class PlayerDataManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePlayer(player: Player) {
        with (sharedPreferences.edit()) {
            putString("test", player.toString())
            apply()
        }
    }

    fun loadPlayer(player: Player){
        val s = sharedPreferences.getString("test", "failed")!!
        player.fromString(s)
    }

//    fun savePlayer(player: Player) {
//        val playerList = loadPlayerList()
//        playerList.add(player)
//        val playerListJson = gson.toJson(playerList)
//        sharedPreferences.edit().putString("player_list", playerListJson).apply()
//    }
//
//    fun loadPlayerList(): MutableList<Player> {
//        val playerListJson = sharedPreferences.getString("player_list", null)
//        return if (playerListJson != null) {
//            gson.fromJson(playerListJson, mutableListOf<Player>()::class.java)
//        } else {
//            mutableListOf()
//        }
//    }
}
