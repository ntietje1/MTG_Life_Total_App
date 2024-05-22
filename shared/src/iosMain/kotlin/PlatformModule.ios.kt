
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectViewModel
import data.ImageManager
import org.koin.dsl.module

actual val platformModule = module {
    single { ImageManager() }
    single { PlayerSelectViewModel() }
    single { LifeCounterViewModel(get(), get(), get()) }
}