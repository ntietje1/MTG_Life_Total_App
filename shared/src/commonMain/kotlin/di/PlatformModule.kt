package di
import data.GameRepository
import domain.storage.LocalSettingsStore
import model.VersionNumber
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val sharedModule = module {
    single { LocalSettingsStore.instance }
    single { BackHandler() }
    single { VersionNumber.current }
    single { DatabaseModule(get()) }
    single<GameRepository> { get<DatabaseModule>().gameRepository }
}

expect val platform: Platform

enum class Platform(val appStoreListing: String) {
    ANDROID("https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"),
    IOS("https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612"),
}