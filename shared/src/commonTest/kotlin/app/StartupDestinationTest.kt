package app

import model.VersionNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class StartupDestinationTest {
    @Test
    fun versionChangeStartsAtSplash() {
        assertEquals(
            LifeLinkedRoute.Splash,
            startupDestination(
                currentVersion = VersionNumber("2.0.0"),
                lastSplashScreenShown = "1.0.0",
                autoSkip = true,
                gameStarted = false
            )
        )
    }

    @Test
    fun matchingVersionWithoutAutoSkipStartsAtPlayerSelect() {
        assertEquals(
            LifeLinkedRoute.PlayerSelect,
            startupDestination(
                currentVersion = VersionNumber("2.0.0"),
                lastSplashScreenShown = "2.0.0",
                autoSkip = false,
                gameStarted = false
            )
        )
    }

    @Test
    fun matchingVersionWithAutoSkipStartsAtLifeCounter() {
        assertEquals(
            LifeLinkedRoute.LifeCounter,
            startupDestination(
                currentVersion = VersionNumber("2.0.0"),
                lastSplashScreenShown = "2.0.0",
                autoSkip = true,
                gameStarted = false
            )
        )
    }

    @Test
    fun matchingVersionWithStartedGameStartsAtLifeCounterEvenWithoutAutoSkip() {
        assertEquals(
            LifeLinkedRoute.LifeCounter,
            startupDestination(
                currentVersion = VersionNumber("2.0.0"),
                lastSplashScreenShown = "2.0.0",
                autoSkip = false,
                gameStarted = true
            )
        )
    }
}
