

import android.app.NotificationManager
import composable.dialog.coinflip.CoinFlipViewModel
import composable.dialog.planechase.PlaneChaseViewModel
import composable.dialog.settings.patchnotes.PatchNotesViewModel
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
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