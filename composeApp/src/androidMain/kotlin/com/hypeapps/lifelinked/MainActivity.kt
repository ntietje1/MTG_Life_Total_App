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
import androidx.compose.runtime.DisposableEffect
import data.SettingsManager


/**
 * Android entry point for the app
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val activity: ComponentActivity = this
        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)
            val root = retainedComponent {
                RootComponent(it)
            }

            LifeLinkedApp(root)

            val flag =WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            DisposableEffect(activity) {
                activity.window.addFlags(flag)
                onDispose {
                    activity.window.clearFlags(flag)
                }
            }
        }
    }
}