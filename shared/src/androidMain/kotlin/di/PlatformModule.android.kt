package di

import data.ImageManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ui.dialog.coinflip.CoinFlipViewModel
import ui.dialog.color.ColorDialogViewModel
import ui.dialog.gif.GifDialogViewModel
import ui.dialog.planechase.PlaneChaseViewModel
import ui.dialog.scryfall.ScryfallSearchViewModel
import ui.dialog.settings.patchnotes.PatchNotesViewModel
import ui.dialog.startinglife.StartingLifeViewModel
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialViewModel

actual val platformModule = module {
    single { platform }
    single { NotificationManager(get()) }
    single { ImageManager(get()) }
    single { PlaneChaseViewModel(get(), get()) } //TODO: workaround for odd lifecycle behavior?
    single { CoinFlipViewModel(get()) }
    viewModel { TutorialViewModel(get()) }
    viewModel { PlayerSelectViewModel(get()) }
    viewModel { LifeCounterViewModel(get(), get(), get()) }
    viewModel { PatchNotesViewModel(get()) }
    viewModel { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel() }
    single { ColorDialogViewModel() }
    single { GifDialogViewModel() }
}

actual val platform: Platform
    get() = Platform.ANDROID