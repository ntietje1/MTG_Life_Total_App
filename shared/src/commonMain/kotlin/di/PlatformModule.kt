package di

import domain.state.game.GameSessionRepository
import domain.state.game.GameSessionStore
import domain.state.game.SavedGameRepository
import domain.game.timer.TimerStateRepository
import domain.state.profile.PlayerProfileRepository
import domain.state.planechase.PlanechaseRepository
import domain.storage.PreferencesRepository
import com.russhwolf.settings.Settings
import model.VersionNumber
import org.koin.core.module.Module
import org.koin.dsl.module
import ui.dialog.settings.patchnotes.PatchNotesRepository

expect val platformModule : Module

val sharedModule = module {
    single { PreferencesRepository(Settings()) }
    single { PlayerProfileRepository(Settings()) }
    single<GameSessionRepository> { SavedGameRepository(Settings(), get()) }
    single { PlanechaseRepository(Settings()) }
    single { TimerStateRepository(Settings()) }
    single { PatchNotesRepository(Settings()) }
    single { GameSessionStore(get<GameSessionRepository>()) }
    single { BackHandler() }
    single { VersionNumber.current }
}

expect val platform: Platform

enum class Platform(val platformString: String, val appStoreListing: String) {
    ANDROID("android", "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"),
    IOS("ios", "https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612"),
}
