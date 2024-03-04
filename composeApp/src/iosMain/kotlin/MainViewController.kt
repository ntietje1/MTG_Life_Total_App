import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.*

fun rootViewController(): UIViewController = ComposeUIViewController {
    val root = remember {
        RootComponent(DefaultComponentContext(LifecycleRegistry()))
    }
    LifeLinkedApp(root)
}