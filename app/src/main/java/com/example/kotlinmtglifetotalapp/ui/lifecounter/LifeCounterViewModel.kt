package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LifeCounterViewModel : ViewModel() {

    // Initialize the player list with an empty list.
    //TODO: change to arraylist of mutablelivedata of player
    private val _playerStates = MutableLiveData<ArrayList<Player>>(arrayListOf())
    val playerStates: LiveData<ArrayList<Player>>
        get() = _playerStates

    init {
        // Initialize the player list with some default players if needed.
        // Example: _players.value = listOf(Player(20, 1, Color.RED), Player(20, 2, Color.BLUE))
    }

    fun acceptPlayerBundle(bundle: Bundle) {

        val players = arrayListOf<Player>()

        val numPlayers = bundle.getInt("numPlayers")
        println("bundle says: $numPlayers players")

        for (i in 1..numPlayers) {

            var player = bundle.getParcelable("P$i", Player::class.java)

            if (player == null) {
                println("Couldn't load player P$i, generating new player")
                player = Player.generatePlayer()

            } else {
                println("Successfully loaded P$i")
            }

            players.add(player)

        }

        updatePlayers(players)
    }

    fun fillPlayerBundle(outState: Bundle) {
        outState.putInt("numPlayers", playerStates.value!!.size)
        for (player in playerStates.value!!) {
            println("Saved $player")
            outState.putParcelable(player.toString(), player)
        }
    }

    // Add a method to update the list of players.
    private fun updatePlayers(newPlayers: ArrayList<Player>) {
        _playerStates.value = newPlayers
    }
}


