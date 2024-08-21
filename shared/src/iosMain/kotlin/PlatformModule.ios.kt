
import ui.dialog.coinflip.CoinFlipViewModel
import ui.dialog.planechase.PlaneChaseViewModel
import ui.dialog.settings.patchnotes.PatchNotesViewModel
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.dsl.module

actual val platformModule = module {
    single { platform }
    single { NotificationManager() }
    single { ImageManager() }
    single { PlaneChaseViewModel(get(), get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel(get()) }
    single { PlayerSelectViewModel(get()) }
    single { LifeCounterViewModel(get(), get(), get()) }
    single { PatchNotesViewModel(get()) }
}

actual val platform: Platform
    get() = Platform.IOS