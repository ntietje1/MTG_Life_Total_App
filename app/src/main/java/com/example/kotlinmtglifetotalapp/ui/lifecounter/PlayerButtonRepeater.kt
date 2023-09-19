package com.example.kotlinmtglifetotalapp.ui.lifecounter

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class PlayerButtonRepeater (private val playerButtonBase: PlayerButtonBase, private val initialDelay: Long, private val repeatDelay: Long) {

    var isRepeating = false

    private var disposable: Disposable? = null

    fun startRepeating(change: Int) {
        playerButtonBase.player!!.increment(change)
        disposable?.dispose()
        disposable = Observable.interval(initialDelay, repeatDelay, TimeUnit.MILLISECONDS)
            .takeWhile { isRepeating }.subscribe({
                playerButtonBase.player!!.increment(change)
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
}