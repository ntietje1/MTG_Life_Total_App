package di
import data.SettingsManager
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val sharedModule = module {
    single { SettingsManager.instance }
    single { BackHandler() }
    single { VersionNumber("1.9.0")  }
}

data class VersionNumber(val value: String) {
    fun isHigherThan(other: VersionNumber): Boolean {
        val thisParts = this.value.split(".").map { it.toInt() }
        val otherParts = other.value.split(".").map { it.toInt() }
        return thisParts[0] > otherParts[0] || (thisParts[0] == otherParts[0] && thisParts[1] > otherParts[1]) || (thisParts[0] == otherParts[0] && thisParts[1] == otherParts[1] && thisParts[2] > otherParts[2])
    }
}

expect val platform: Platform

enum class Platform(val platformString: String, val appStoreListing: String) {
    ANDROID("android", "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"),
    IOS("ios", "https://apps.apple.com/us/app/lifelinked-mtg-life-counter/id6503708612"),
}


expect class NotificationManager {
    fun showNotification(message: String, duration: Long = 2000L)
}