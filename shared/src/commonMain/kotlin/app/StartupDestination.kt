package app

import model.VersionNumber

fun startupDestination(
    currentVersion: VersionNumber,
    lastSplashScreenShown: String,
    autoSkip: Boolean
): LifeLinkedRoute {
    return if (!currentVersion.isSame(VersionNumber(lastSplashScreenShown))) {
        LifeLinkedRoute.Splash
    } else if (!autoSkip) {
        LifeLinkedRoute.PlayerSelect
    } else {
        LifeLinkedRoute.LifeCounter
    }
}
