package domain.game

import androidx.compose.ui.graphics.Color
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerProfileRepository
import domain.storage.TestSettings
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
