package com.example.kotlinmtglifetotalapp.ui.lifecounter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LifeCounterViewModel : ViewModel() {

    private val _playerStates = MutableLiveData<MutableList<Player>>()
    private val playerStates: LiveData<MutableList<Player>> = _playerStates

    init {
        _playerStates.value = mutableListOf() // Initialize the list
        for (i in 0..4) {
            playerStates.value?.add(Player.generatePlayer())
        }
    }

    fun savePlayerState(player: Player) {
        val currentList = _playerStates.value ?: mutableListOf()
        currentList.add(player)
        _playerStates.value = currentList
    }

    fun loadPlayerStates(): List<Player> {
        return _playerStates.value ?: emptyList()
    }

    fun clearPlayerStates() {
        _playerStates.value?.clear()
    }
}
