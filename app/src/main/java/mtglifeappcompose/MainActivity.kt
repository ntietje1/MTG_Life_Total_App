package mtglifeappcompose

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import mtglifeappcompose.composable.MTGLifeTotalApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)
            MTGLifeTotalApp()
        }
    }
}

//        enableEdgeToEdge()

//        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
//
//        insetsController.apply {
//            hide(WindowInsetsCompat.Type.statusBars())
//            hide(WindowInsetsCompat.Type.navigationBars())
//            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }