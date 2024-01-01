package lifelinked

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import lifelinked.composable.LifeLinkedApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContent {
            window.decorView.setBackgroundColor(if (isSystemInDarkTheme()) Color.BLACK else Color.WHITE)
            LifeLinkedApp()
        }
    }
}
