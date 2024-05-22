package com.hypeapps.lifelinked


import LifeLinkedApp
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val activity: ComponentActivity = this
        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)
            LifeLinkedApp()
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            DisposableEffect(activity) {
                activity.window.addFlags(flag)
                onDispose {
                    activity.window.clearFlags(flag)
                }
            }
        }
    }
}