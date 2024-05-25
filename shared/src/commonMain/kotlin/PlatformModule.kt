
import data.SettingsManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val sharedModule = module {
    single { SettingsManager.instance }
    single { BackHandler() }
}

expect val platform: Platform

enum class Platform(val platformString: String) {
    ANDROID("android"),
    IOS("ios"),
}