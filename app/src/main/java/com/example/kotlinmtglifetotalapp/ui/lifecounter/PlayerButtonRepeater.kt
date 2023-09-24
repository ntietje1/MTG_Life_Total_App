package com.example.kotlinmtglifetotalapp.ui.lifecounter

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class PlayerButtonRepeater (private val playerButtonBase: PlayerButtonBase, private val initialDelay: Long, private val repeatDelay: Long) {

    var isRepeating = false

    private var disposable: Disposable? = null

    fun startRepeating(change: Int) {
        increment(change)
        disposable?.dispose()
        disposable = Observable.interval(initialDelay, repeatDelay, TimeUnit.MILLISECONDS)
            .takeWhile { isRepeating }.subscribe({
                increment(change)
            }, {
                it.printStackTrace()
            })
        isRepeating = true
    }

    fun stopRepeating() {
        disposable?.dispose()
        disposable = null
        isRepeating = false
    }

    private fun increment(change: Int) {
        val player = playerButtonBase.player!!
        when (playerButtonBase.state) {
            PlayerButtonState.NORMAL -> player.incrementLife(change)
            PlayerButtonState.COMMANDER_RECEIVER -> {
                player.incrementCommander(change)
                PlayerButtonBase.currCommanderDamage[player.playerNum - 1] += change
            }
            PlayerButtonState.COMMANDER_DEALER -> Unit
        }
    }
}