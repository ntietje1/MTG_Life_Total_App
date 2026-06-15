package app

import model.VersionNumber

fun startupDestination(
    currentVersion: VersionNumber,
    lastSplashScreenShown: String,
    autoSkip: Boolean,
    gameStarted: Boolean
): LifeLinkedRoute {
    return if (!currentVersion.isSame(VersionNumber(lastSplashScreenShown))) {
        LifeLinkedRoute.Splash
    } else if (gameStarted || autoSkip) {
        LifeLinkedRoute.LifeCounter
    } else {
        LifeLinkedRoute.PlayerSelect
    }
}
