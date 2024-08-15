
import composable.dialog.coinflip.CoinFlipViewModel
import composable.dialog.planechase.PlaneChaseViewModel
import composable.dialog.settings.patchnotes.PatchNotesViewModel
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.dsl.module

actual val platformModule = module {
    single { platform }
    single { ImageManager() }
    single { PlaneChaseViewModel(get(), get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel(get()) }
    single { PlayerSelectViewModel(get()) }
    single { LifeCounterViewModel(get(), get(), get()) }
    single { PatchNotesViewModel() }
}

actual val platform: Platform
    get() = Platform.IOS