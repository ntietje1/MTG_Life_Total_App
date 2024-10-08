package di
import data.ImageManager
import org.koin.dsl.module
import ui.dialog.coinflip.CoinFlipViewModel
import ui.dialog.planechase.PlaneChaseViewModel
import ui.dialog.scryfall.ScryfallSearchViewModel
import ui.dialog.settings.patchnotes.PatchNotesViewModel
import ui.dialog.startinglife.StartingLifeViewModel
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialViewModel

actual val platformModule = module {
    single { platform }
    single { di.NotificationManager() }
    single { ImageManager() }
    single { PlaneChaseViewModel(get(), get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel(get()) }
    single { PlayerSelectViewModel(get()) }
    single { LifeCounterViewModel(get(), get(), get()) }
    single { PatchNotesViewModel(get()) }
    single { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel() }
}

actual val platform: Platform
    get() = Platform.IOS