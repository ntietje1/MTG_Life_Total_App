
import composable.dialog.planechase.PlaneChaseViewModel
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

actual val platformModule = module {
    single { platform }
    single { ImageManager(get()) }
    single { PlaneChaseViewModel(get(), get()) } //TODO: workaround for odd lifecycle behavior?
    viewModel { TutorialViewModel(get()) }
    viewModel { PlayerSelectViewModel(get()) }
    viewModel { LifeCounterViewModel(get(), get()) }
}

actual val platform: Platform
    get() = Platform.ANDROID