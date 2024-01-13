import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController() = ComposeUIViewController { App() }

//fun MainViewController(): UIViewController =
//    ComposeUIViewController {
//        ImageViewerIos()
//    }
