package di
import domain.storage.FileImageStore
import domain.storage.IFileImageStore
import domain.game.PlayerCustomizationManager
import domain.game.timer.TimerManager
import domain.system.NotificationManager
import org.koin.dsl.module
import ui.dialog.coinflip.CoinFlipViewModel
import ui.dialog.color.ColorDialogViewModel
import ui.dialog.dice.DiceRollViewModel
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
    single { NotificationManager() }
    single<IFileImageStore> { FileImageStore() }
    single { PlayerCustomizationManager(get()) }
    single { TimerManager(timerStateRepository = get(), preferencesRepository = get()) }
    single { PlaneChaseViewModel(planechaseRepository = get(), scryfallClient = get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel() }
    single { PlayerSelectViewModel(get()) }
    single { LifeCounterViewModel(
        preferencesRepository = get(),
        profileRepository = get(),
        fileImageStore = get(),
        notificationManager = get(),
        playerCustomizationManager = get(),
        planeChaseViewModel = get(),
        gameSessionStore = get(),
        timerManager = get()
    )  }
    single { PatchNotesViewModel(patchNotesRepository = get(), preferencesRepository = get()) }
    single { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel(get()) }
    single { ColorDialogViewModel() }
    single { GifDialogViewModel(get()) }
    single { DiceRollViewModel() }
}

actual val platform: Platform
    get() = Platform.IOS
