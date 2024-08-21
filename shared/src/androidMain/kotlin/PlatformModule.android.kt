

import android.app.NotificationManager
import ui.dialog.coinflip.CoinFlipViewModel
import ui.dialog.planechase.PlaneChaseViewModel
import ui.dialog.settings.patchnotes.PatchNotesViewModel
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

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
}

actual val platform: Platform
    get() = Platform.ANDROID