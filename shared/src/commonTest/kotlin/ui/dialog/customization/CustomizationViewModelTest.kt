package ui.dialog.customization

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

    private fun customizationViewModel(
        onPlayerChanged: (Player) -> Unit = {}
    ): CustomizationViewModel {
        return CustomizationViewModel(
            initialPlayer = Player(playerNum = 1),
            fileImageStore = FakeFileImageStore(),
            profileRepository = PlayerProfileRepository(TestSettings()),
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
