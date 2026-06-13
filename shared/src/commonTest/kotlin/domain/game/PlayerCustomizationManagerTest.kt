package domain.game

import androidx.compose.ui.graphics.Color
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerProfileRepository
import domain.storage.TestSettings
import model.Player
import model.Player.Companion.allPlayerColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlayerCustomizationManagerTest {
    @Test
    fun savesTypedPlayerBackgroundInsteadOfDisplayUri() {
        val repository = PlayerProfileRepository(
            settings = TestSettings()
        )
        val manager = PlayerCustomizationManager(repository)

        manager.savePlayerPrefs(
            Player(
                name = "Nissa",
                color = Color(1),
                textColor = Color(2),
                imageString = "file:///images/local-id",
                background = PlayerBackground.LocalImage("local-id")
            )
        )

        assertEquals(PlayerBackground.LocalImage("local-id"), repository.loadProfiles().single().background)
    }

    @Test
    fun resetsPlayerPrefsUsingHostPlayerColorsWithoutPlayerButtonViewModels() {
        val repository = PlayerProfileRepository(
            settings = TestSettings()
        )
        val manager = PlayerCustomizationManager(repository)
        val unusedColor = allPlayerColors.last()
        val host = FakePlayerCustomizationHost(
            players = allPlayerColors.dropLast(1).mapIndexed { index, color ->
                Player(playerNum = index + 1, color = color)
            }
        )
        manager.attach(host)

        val reset = manager.resetPlayerPrefs(host.players.first())

        assertEquals(unusedColor, reset.color)
        assertNotEquals(host.players.first().color, reset.color)
    }
}

private class FakePlayerCustomizationHost(
    override val players: List<Player>
) : PlayerCustomizationHost {
    override fun replacePlayer(player: Player) {
    }
}
