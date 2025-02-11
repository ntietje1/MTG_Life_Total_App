package domain.usecase.player

import domain.usecase.player.state.ManagePlayerCounterUseCase
import domain.usecase.player.state.ManagePlayerStateUseCase
import domain.usecase.player.state.NewPlayerStateUseCase
import domain.usecase.player.state.SavePlayerStateUseCase
import org.koin.dsl.module

val playerModule = module {
    factory { ManagePlayerStateUseCase(get(), get()) }
    factory { ManagePlayerCounterUseCase() }
    factory { SavePlayerStateUseCase(get()) }
    factory { NewPlayerStateUseCase(get()) }
    factory { NewPlayerUseCase(get()) }
}