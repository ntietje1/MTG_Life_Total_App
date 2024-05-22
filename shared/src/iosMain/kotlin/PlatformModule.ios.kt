
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import composable.tutorial.TutorialViewModel
import data.ImageManager
import org.koin.dsl.module

actual val platformModule = module {
    single { ImageManager() }
    single { TutorialViewModel() }
    single { PlayerSelectViewModel() }
    single { LifeCounterViewModel(get(), get()) }
}