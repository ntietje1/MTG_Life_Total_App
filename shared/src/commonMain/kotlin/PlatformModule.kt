
import data.SettingsManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val sharedModule = module {
    single { SettingsManager.instance }
    single { BackHandler() }
}

expect val platform: Platform

enum class Platform(val platformString: String, val appStoreListing: String) {
    ANDROID("android", "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"),
    IOS("ios", "NOT YET IMPLEMENTED"),
}