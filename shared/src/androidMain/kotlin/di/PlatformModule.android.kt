package di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hypeapps.lifelinked.db.Database
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import domain.storage.ImageManager
import domain.storage.SettingsManager
import domain.game.GameStateManager
import domain.game.CommanderDamageManager
import domain.game.PlayerCustomizationManager
import domain.game.PlayerStateManager
import domain.game.timer.TimerManager
import domain.system.NotificationManager
import domain.system.SystemManager
import org.koin.core.module.dsl.viewModel
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
    single { NotificationManager(get()) }
    single<ISettingsManager> { SettingsManager.instance }
    single<IImageManager> { ImageManager(get()) }
    single { PlayerStateManager(get()) }
    single { PlayerCustomizationManager(get()) }
    single { CommanderDamageManager(get()) }
    single { GameStateManager(get(), get()) }
    single { TimerManager(get()) }
    single { PlaneChaseViewModel(get()) }
    single { CoinFlipViewModel(get()) }
    viewModel { TutorialViewModel(get()) }
    viewModel { PlayerSelectViewModel(get(), get()) }
    viewModel { 
        LifeCounterViewModel(
            settingsManager = get(),
            playerStateManager = get(),
            commanderManager = get(), 
            imageManager = get(),
            notificationManager = get(),
            playerCustomizationManager = get(),
            planeChaseViewModel = get(),
            gameStateManager = get(),
            timerManager = get()
        ) 
    }
    viewModel { PatchNotesViewModel(get()) }
    viewModel { StartingLifeViewModel(get()) }
    single { ScryfallSearchViewModel() }
    single { ColorDialogViewModel() }
    single { GifDialogViewModel() }
    viewModel { DiceRollViewModel() }
    single<SqlDriver> { AndroidSqliteDriver(
            schema = Database.Schema,
            context = get(),
            name = "lifelinked.db"
        )
    }
}

actual val platform: Platform
    get() = Platform.ANDROID