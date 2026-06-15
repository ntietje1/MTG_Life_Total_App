package com.hypeapps.lifelinked

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import domain.api.GifAsset
import domain.api.GifPage
import domain.api.GifProvider
import domain.api.GifSearchClient
import domain.api.GifSearchResult
import domain.api.ScryfallClient
import domain.api.ScryfallPage
import domain.api.ScryfallResult
import domain.storage.PreferencesRepository
import model.card.CardArt
import model.card.CardSummary
import model.card.RulingSummary
import model.VersionNumber
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import ui.dialog.planechase.PlanarDieResult
import ui.dialog.planechase.PlaneChaseViewModel

class LifeLinkedTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, LifeLinkedTestApp::class.java.name, context)
    }
}

class LifeLinkedTestApp : App() {
    override fun onCreate() {
        super.onCreate()
        seedE2EPreferences()
        loadKoinModules(
            module {
                single<GifSearchClient> { FakeGifSearchClient() }
                single<ScryfallClient> { FakeScryfallClient() }
                single {
                    PlaneChaseViewModel(
                        planechaseRepository = get(),
                        scryfallClient = get(),
                        planarDieRoll = { PlanarDieResult.PLANESWALK }
                    )
                }
            }
        )
    }

    private fun seedE2EPreferences() {
        val koin = GlobalContext.get()
        val preferences = koin.get<PreferencesRepository>()
        val version = koin.get<VersionNumber>()
        preferences.setLastSplashScreenShown(version.value)
        preferences.setTutorialSkip(true)
        preferences.setAutoSkip(true)
    }
}

private class FakeGifSearchClient : GifSearchClient {
    override suspend fun search(query: String, limit: Int, cursor: String?): GifSearchResult {
        return GifSearchResult.Success(
            GifPage(
                items = listOf(
                    GifAsset(
                        id = "e2e-gif",
                        previewUrl = "https://example.test/e2e-preview.gif",
                        fullUrl = "https://example.test/e2e-full.gif",
                        width = 320,
                        height = 180,
                        provider = GifProvider.KLIPY,
                        attributionUrl = "https://example.test/e2e"
                    )
                ),
                nextCursor = null
            )
        )
    }
}

private class FakeScryfallClient : ScryfallClient {
    override suspend fun searchCards(query: String): ScryfallResult<ScryfallPage> {
        return ScryfallResult.Success(ScryfallPage(cards = listOf(fakeCardFor(query)), nextPageUrl = null))
    }

    override suspend fun searchRulings(query: String): ScryfallResult<List<RulingSummary>> {
        return ScryfallResult.Success(emptyList())
    }

    private fun fakeCardFor(query: String): CardSummary {
        if (query.contains("t:plane") || query.contains("t:phenomenon")) {
            return CardSummary(
                id = "e2e-plane",
                name = "E2E Plane",
                art = CardArt(
                    small = "https://example.test/e2e-plane-small.jpg",
                    normal = "https://example.test/e2e-plane-normal.jpg",
                    large = "https://example.test/e2e-plane-large.jpg",
                    artCrop = "https://example.test/e2e-plane-art.jpg"
                ),
                artist = "E2E Artist",
                setName = "E2E Plane Set",
                printsSearchUri = "https://api.scryfall.com/cards/search?q=e2e-plane",
                rulingsUri = "https://api.scryfall.com/cards/e2e-plane/rulings"
            )
        }

        return CardSummary(
            id = "e2e-card",
            name = "E2E Card",
            art = CardArt(
                small = "https://example.test/e2e-card-small.jpg",
                normal = "https://example.test/e2e-card-normal.jpg",
                large = "https://example.test/e2e-card-large.jpg",
                artCrop = "https://example.test/e2e-card-art.jpg"
            ),
            artist = "E2E Artist",
            setName = "E2E Set",
            printsSearchUri = "https://api.scryfall.com/cards/search?q=e2e",
            rulingsUri = "https://api.scryfall.com/cards/e2e/rulings"
        )
    }
}
