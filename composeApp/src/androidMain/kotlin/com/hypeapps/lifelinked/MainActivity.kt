package com.hypeapps.lifelinked


import RootComponent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import LifeLinkedApp
import data.SettingsManager


/**
 * Android entry point for the app
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)
            if (SettingsManager.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            val root = retainedComponent {
                RootComponent(it)
            }
            LifeLinkedApp(root)
        }
    }
}