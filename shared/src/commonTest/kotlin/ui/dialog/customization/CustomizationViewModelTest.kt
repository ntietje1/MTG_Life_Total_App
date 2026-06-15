package ui.dialog.customization

import androidx.compose.ui.graphics.Color
import domain.state.game.PlayerProfileId
import domain.state.profile.PlayerProfile
import domain.state.profile.PlayerColors
import domain.state.profile.PlayerProfileRepository
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.storage.TestSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import model.Player

class CustomizationViewModelTest {
    @Test
    fun pushesAndPopsTypedCustomizationRoutes() {
        val viewModel = customizationViewModel()

        viewModel.openRoute(CustomizationRoute.ScryfallSearch)

        assertEquals(CustomizationRoute.ScryfallSearch, viewModel.state.value.currentRoute)
        assertTrue(viewModel.goBack())
        assertEquals(CustomizationRoute.Default, viewModel.state.value.currentRoute)
        assertFalse(viewModel.goBack())
    }

    @Test
    fun resetRouteStackReturnsToDefaultRoute() {
        val viewModel = customizationViewModel()

        viewModel.openRoute(CustomizationRoute.LoadPlayer)
        viewModel.openRoute(CustomizationRoute.GifSearch)
        viewModel.resetRouteStack()

        assertEquals(listOf(CustomizationRoute.Default), viewModel.state.value.routeStack)
    }

    @Test
    fun notifiesWhenPlayerChanges() {
        val changedPlayers = mutableListOf<Player>()
        val viewModel = customizationViewModel(onPlayerChanged = changedPlayers::add)

        viewModel.setPlayer(Player(playerNum = 1, name = "Tutorial"))

        assertEquals("Tutorial", changedPlayers.single().name)
    }

    @Test
    fun loadPlayerPrefsHidesDefaultPlayerProfiles() {
        val profileRepository = PlayerProfileRepository(TestSettings())
        profileRepository.saveProfile(PlayerProfile(PlayerProfileId("P1"), "P1", PlayerColors(1, 2)))
        profileRepository.saveProfile(PlayerProfile(PlayerProfileId("Jace"), "Jace", PlayerColors(3, 4)))
        val viewModel = customizationViewModel(profileRepository = profileRepository)

        val players = viewModel.loadPlayerPrefs()

        assertEquals(listOf("Jace"), players.map { it.name })
        assertEquals(Color(3), players.single().color)
    }

    private fun customizationViewModel(
        profileRepository: PlayerProfileRepository = PlayerProfileRepository(TestSettings()),
        onPlayerChanged: (Player) -> Unit = {}
    ): CustomizationViewModel {
        return CustomizationViewModel(
            initialPlayer = Player(playerNum = 1),
            fileImageStore = FakeFileImageStore(),
            profileRepository = profileRepository,
            preferencesRepository = PreferencesRepository(TestSettings()),
            onPlayerChanged = onPlayerChanged
        )
    }
}

private class FakeFileImageStore : IFileImageStore {
    override suspend fun saveImage(bytes: ByteArray): String = "image-id"
    override fun localImageUri(imageId: String): String? = null
    override fun deleteImage(imageId: String) = Unit
}
