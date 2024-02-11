import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.*

fun rootViewController(root: RootComponent): UIViewController = ComposeUIViewController {
    LifeLinkedApp(root)
}