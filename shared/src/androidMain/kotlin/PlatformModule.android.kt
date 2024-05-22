
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

actual val platformModule = module {
    single { ImageManager(get()) }
    viewModel { TutorialViewModel() }
    viewModel { PlayerSelectViewModel() }
    viewModel { LifeCounterViewModel(get(), get()) }
}