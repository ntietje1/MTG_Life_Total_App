
import composable.dialog.planechase.PlaneChaseViewModel
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.dsl.module

actual val platformModule = module {
    single { platform }
    single { ImageManager() }
    single { PlaneChaseViewModel(get(), get()) }
    single { TutorialViewModel() }
    single { PlayerSelectViewModel() }
    single { LifeCounterViewModel(get(), get()) }
}

actual val platform: Platform
    get() = Platform.IOS