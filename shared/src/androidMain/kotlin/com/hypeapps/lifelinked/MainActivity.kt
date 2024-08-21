package com.hypeapps.lifelinked


import di.BackHandler
import app.LifeLinkedApp
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val backHandler: BackHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val activity: ComponentActivity = this
//        enableEdgeToEdge()

        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }

//            fun generateCallback (): () -> Unit {
//                return {
//                    println("back button pressed!!")
//                    backHandler.pop()
//                }
//            }

            this.onBackPressedDispatcher.addCallback(this) {
                println("back button pressed!1")
//                this.isEnabled = false
                backHandler.pop()
//                this.handleOnBackPressed()
//                this.remove()
//                onBackPressedDispatcher.addCallback(this@MainActivity) {
//                    generateCallback().invoke()
//                }
            }

            LifeLinkedApp()
        }
    }
}