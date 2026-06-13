package di

import domain.state.game.GameSessionRepository
import domain.state.game.GameSessionStore
import domain.state.legacy.LocalGameSessionRepository
import domain.storage.SettingsManager
import model.VersionNumber
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val sharedModule = module {
    single { SettingsManager.instance }
    single<GameSessionRepository> { LocalGameSessionRepository(get<SettingsManager>()) }
    single { GameSessionStore(get<GameSessionRepository>()) }
    single { BackHandler() }
    single { VersionNumber.current }
}

expect val platform: Platform

enum class Platform(val platformString: String, val appStoreListing: String) {
    ANDROID("android", "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"),
    IOS("ios", "https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612"),
}
