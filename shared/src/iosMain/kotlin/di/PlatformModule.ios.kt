package di
import data.IImageManager
import data.ISettingsManager
import data.ImageManager
import data.SettingsManager
import domain.player.CommanderDamageManager
import domain.player.CounterManager
import domain.game.GameStateManager
import domain.player.PlayerCustomizationManager
import domain.player.PlayerManager
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
    single<ISettingsManager> { SettingsManager.instance }
    single<IImageManager> { ImageManager() }
    single { PlayerManager(get(), get()) }
    single { PlayerCustomizationManager() }
    single { GameStateManager() }
    single { CommanderDamageManager(get()) }
    single { CounterManager() }
    single { NotificationManager() }
    single { PlaneChaseViewModel(get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel(get()) }
    single { PlayerSelectViewModel(get()) }
    single { LifeCounterViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PatchNotesViewModel(get()) }
    single { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel() }
    single { ColorDialogViewModel() }
    single { GifDialogViewModel() }
}

actual val platform: Platform
    get() = Platform.IOS