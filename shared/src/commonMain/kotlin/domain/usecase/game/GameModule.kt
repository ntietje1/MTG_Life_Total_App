package domain.usecase.game

import org.koin.dsl.module

val gameModule = module {
    factory { NewGameUseCase(get(), get()) }
    factory { SaveGameUseCase(get()) }
    factory { LoadGameStateUseCase(get()) }
}