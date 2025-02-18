package domain.usecase.player

import domain.usecase.player.customization.DeletePlayerCustomizationUseCase
import domain.usecase.player.customization.LoadPlayerCustomizationUseCase
import domain.usecase.player.customization.ManagePlayerCustomizationUseCase
import domain.usecase.player.customization.ResetPlayerCustomizationUseCase
import domain.usecase.player.customization.SavePlayerCustomizationUseCase
import domain.usecase.player.state.ManagePlayerCounterUseCase
import domain.usecase.player.state.ManagePlayerStateUseCase
import domain.usecase.player.state.NewPlayerStateUseCase
import domain.usecase.player.state.SavePlayerStateUseCase
import org.koin.dsl.module

val playerModule = module {
    factory { ManagePlayerStateUseCase(get(), get(), get()) }
    factory { ManagePlayerCounterUseCase() }
    factory { SavePlayerStateUseCase(get()) }
    factory { NewPlayerStateUseCase(get()) }

    factory { LoadPlayerCustomizationUseCase(get(), get()) }
    factory { SavePlayerCustomizationUseCase(get()) }
    factory { DeletePlayerCustomizationUseCase(get()) }
    factory { ResetPlayerCustomizationUseCase() }
    factory { ManagePlayerCustomizationUseCase(get()) }

    factory { NewPlayerUseCase(get(), get()) }
}