package di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.hypeapps.lifelinked.db.Database
import domain.game.CommanderDamageManager
import domain.game.PlayerCustomizationManager
import domain.game.timer.TimerManager
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import domain.storage.ImageManager
import domain.storage.SettingsManager
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
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialViewModel

actual val platformModule = module {
    single { platform }
    single { NotificationManager() }
    single<ISettingsManager> { SettingsManager.instance }
    single<IImageManager> { ImageManager() }
    single { PlayerCustomizationManager(get(), get()) }
    single { CommanderDamageManager(get()) }
    single { TimerManager(get()) }
    single { PlaneChaseViewModel(get()) }
    single { CoinFlipViewModel(get()) }
    single { TutorialViewModel(get()) }
    single { PlayerSelectViewModel(get()) }
    single {
        LifeCounterViewModel(
            settingsManager = get(),
            commanderManager = get(),
            imageManager = get(),
            notificationManager = get(),
            playerCustomizationManager = get(),
            planeChaseViewModel = get(),
            newGameUseCase = get(),
            saveGameUseCase = get(),
            loadGameStateUseCase = get(),
            monarchyState = get(),
            managePlayerStateUseCase = get(),
            timerManager = get(),
            newPlayerUseCase = get()
        )
    }
    factory { (initialState: PlayerButtonState) ->
        PlayerButtonViewModel(
            initialState = initialState,
            settingsManager = get(),
            imageManager = get(),
            playerCustomizationManager = get(),
            managePlayerStateUseCase = get(),
            savePlayerStateUseCase = get(),
            playerLifeRecentChangeState = get(),
            commanderManager = get(),
            notificationManager = get(),
            timerManager = get()
        )
    }
    single { PatchNotesViewModel(get()) }
    single { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel() }
    single { ColorDialogViewModel() }
    single { GifDialogViewModel() }
    single { DiceRollViewModel() }
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = Database.Schema,
            name = "lifelinked.db"
        )
    }
}

actual val platform: Platform
    get() = Platform.IOS