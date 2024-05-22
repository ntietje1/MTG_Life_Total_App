package com.hypeapps.lifelinked

import KoinInitializer
import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinInitializer(applicationContext).init()
    }
}