package mtglifeappcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import mtglifeappcompose.ui.theme.MTGLifeAppComposeTheme
import mtglifeappcompose.views.MTGLifeTotalApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MTGLifeAppComposeTheme(darkTheme = true) {
                MTGLifeTotalApp()
            }
//            MTGLifeAppComposeTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
        }
    }
}