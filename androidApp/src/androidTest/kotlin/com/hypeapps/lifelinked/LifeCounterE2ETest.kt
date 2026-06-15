package com.hypeapps.lifelinked

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import domain.game.timer.TimerStateRepository
import domain.state.game.GameCommand
import domain.state.game.GameMutation
import domain.state.game.GameRules
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.GameSessionRepository
import domain.state.game.GameSessionStore
import domain.state.game.PlayerProfileId
import domain.state.game.SeatAppearance
import domain.state.planechase.PlanechaseRepository
import domain.state.planechase.PlanechaseSnapshot
import domain.state.profile.PlayerProfile
import domain.state.profile.PlayerProfileRepository
import domain.storage.PreferencesRepository
import kotlinx.coroutines.runBlocking
import model.VersionNumber
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import theme.PlayerColor6
import theme.PlayerColor7
import ui.dialog.planechase.PlaneChaseViewModel

@RunWith(AndroidJUnit4::class)
class LifeCounterE2ETest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetE2EPreferences() {
        val koin = GlobalContext.get()
        val preferences = koin.get<PreferencesRepository>()
        val version = koin.get<VersionNumber>()
        preferences.setLastSplashScreenShown(version.value)
        preferences.setTutorialSkip(true)
        preferences.setFastCoinFlip(false)
        preferences.setCameraRollDisabled(false)
        preferences.setAutoKo(true)
        preferences.setAutoSkip(true)
        preferences.setKeepScreenOn(false)
        preferences.setTurnTimer(false)
        preferences.setNumPlayers(4)
        preferences.setAlt4PlayerLayout(false)
        preferences.setDarkTheme(true)
        preferences.setStartingLife(40)
        preferences.setDevMode(false)
        koin.get<TimerStateRepository>().save(null)
        runBlocking {
            val freshSession = GameSession.newGame(
                id = GameSessionId("local-active-game"),
                rules = GameRules(startingLife = preferences.startingLife.value),
                appearances = (1..preferences.numPlayers.value).map { seatNumber ->
                    SeatAppearance(displayName = "P$seatNumber")
                }
            )
            koin.get<GameSessionRepository>().commit(
                GameMutation(
                    sessionId = freshSession.id,
                    expectedVersion = freshSession.version,
                    command = GameCommand.ResetGame(),
                    resultingSession = freshSession
                )
            )
            koin.get<GameSessionStore>().loadActiveSession()
        }
        koin.get<PlanechaseRepository>().save(PlanechaseSnapshot())
        koin.get<PlaneChaseViewModel>().run {
            removeAllPlanarDeck(state.value.planarDeck.toList())
        }
        composeRule.activityRule.scenario.recreate()
        val profileRepository = koin.get<PlayerProfileRepository>()
        profileRepository.loadProfiles().forEach { profile ->
            profileRepository.deleteProfile(profile.id)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scryfallSearchUsesInjectedClient() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()

        waitForContentDescription(OPEN_CARD_IMAGE_SEARCH)
        composeRule.onNodeWithContentDescription(OPEN_CARD_IMAGE_SEARCH, useUnmergedTree = true)
            .performTouchInput { click() }

        searchScryfallForTestCard()
        waitForContentDescription(TEST_CARD_SELECT)
        composeRule.onNodeWithContentDescription(TEST_CARD_SELECT, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)
        closeCustomizationAndReturnToCounter()
        verifyP1StatePersistsAfterLifeChange("$P1_BACKGROUND_IMAGE_PREFIX$TEST_CARD_IMAGE_URI")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scryfallDetailsCanShowRulingsAndPrintings() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_CARD_SEARCH)

        searchScryfallForTestCard()
        performSemanticClick(SHOW_TEST_CARD_RULINGS)
        waitForText(TEST_RULING_COMMENT)

        performSemanticClick(BACK_IN_DIALOG)
        waitForText(TEST_CARD_NAME)

        performSemanticClick(SHOW_TEST_CARD_PRINTINGS)
        waitForText(TEST_PRINTING_CARD_NAME)

        performSemanticClick(BACK_IN_DIALOG)
        waitForText(TEST_CARD_NAME)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationScryfallPrintingsBackReturnsToCustomization() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        performSemanticClick(OPEN_CARD_IMAGE_SEARCH)

        searchScryfallForTestCard()
        performSemanticClick(SHOW_TEST_CARD_PRINTINGS)
        waitForText(TEST_PRINTING_CARD_NAME)

        performSemanticClick(BACK_IN_DIALOG)
        waitForText(TEST_CARD_NAME)

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gifSearchUsesInjectedClient() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()

        waitForContentDescription(OPEN_GIF_SEARCH)
        composeRule.onNodeWithContentDescription(OPEN_GIF_SEARCH, useUnmergedTree = true)
            .performTouchInput { click() }

        searchGifForTestResult()
        composeRule.onAllNodesWithContentDescription(TEST_GIF_RESULT, useUnmergedTree = true)[0]
            .performTouchInput { click() }

        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)
        closeCustomizationAndReturnToCounter()
        verifyP1StatePersistsAfterLifeChange("$P1_BACKGROUND_IMAGE_PREFIX$TEST_GIF_IMAGE_URI")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationGifSearchBackReturnsToCustomization() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        performSemanticClick(OPEN_GIF_SEARCH)

        searchGifForTestResult()

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationUploadWarningCancelReturnsToCustomization() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        performSemanticClick(UPLOAD_PLAYER_IMAGE)
        waitForText(CAMERA_ROLL_WARNING)

        performSemanticClick(CANCEL_WARNING)
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loadProfileDeleteWarningCancelKeepsProfile() {
        GlobalContext.get().get<PlayerProfileRepository>().saveProfile(
            PlayerProfile(PlayerProfileId(SAVED_PROFILE_NAME), SAVED_PROFILE_NAME)
        )

        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        performSemanticClick(OPEN_LOAD_PROFILE)
        waitForContentDescription("$LOAD_PROFILE_PREFIX$SAVED_PROFILE_NAME")

        composeRule.onNodeWithContentDescription("$LOAD_PROFILE_PREFIX$SAVED_PROFILE_NAME", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        waitForText(DELETE_PROFILE_WARNING)

        performSemanticClick(CANCEL_WARNING)
        waitForContentDescription("$LOAD_PROFILE_PREFIX$SAVED_PROFILE_NAME")

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tutorialCanOpenNavigateAndSkip() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_APP_SETTINGS)
        performSemanticClick(OPEN_TUTORIAL_FROM_SETTINGS)

        waitForContentDescription(SKIP_TUTORIAL)
        performSemanticClick(TUTORIAL_GO_FORWARD)
        waitForContentDescription(TUTORIAL_GO_BACK)

        performSemanticClick(TUTORIAL_GO_BACK)
        waitForContentDescription(TUTORIAL_GO_FORWARD)

        performSemanticClick(SKIP_TUTORIAL)
        waitForContentDescription(CONFIRM_SKIP_TUTORIAL)
        performSemanticClick(CONFIRM_SKIP_TUTORIAL)

        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleMenuSettingsTogglesCanBeChanged() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenu()
        val initialTheme = readContentDescriptionValue(THEME_STATE_PREFIX)
        performSemanticClick(TOGGLE_THEME)
        waitUntil("theme changed") {
            findContentDescriptionValue(THEME_STATE_PREFIX)
                ?.let { it != initialTheme } == true
        }

        val initialDayNight = readContentDescriptionValue(DAY_NIGHT_STATE_PREFIX)
        performSemanticClick(TOGGLE_DAY_NIGHT)
        waitUntil("day night changed") {
            findContentDescriptionValue(DAY_NIGHT_STATE_PREFIX)
                ?.let { it != initialDayNight } == true
        }

        performSemanticClick(OPEN_APP_SETTINGS)

        verifySettingToggleChanges(FAST_COIN_FLIP_SETTING_PREFIX)
        verifySettingToggleChanges(DISABLE_CAMERA_ROLL_SETTING_PREFIX)
        verifySettingToggleChanges(AUTO_KO_SETTING_PREFIX)
        verifySettingToggleChanges(AUTO_SKIP_PLAYER_SELECT_SETTING_PREFIX)
        verifySettingToggleChanges(KEEP_SCREEN_ON_SETTING_PREFIX)
        verifySettingToggleChanges(TURN_TIMER_SETTING_PREFIX)

        composeRule.onNodeWithContentDescription(CLOSE_DIALOG, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun settingsChildDialogCanBackAndClose() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_APP_SETTINGS)
        performSemanticClick(OPEN_PATCH_NOTES)
        waitForText(CHANGE_LOG_TITLE)

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(OPEN_TUTORIAL_FROM_SETTINGS)

        performSemanticClick(CLOSE_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun turnTimerCanSelectFirstPlayerAndMoveToNextPlayer() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        enableTurnTimerAndSelectP1()

        waitForContentDescription(P1_ACTIVE_TURN_TIMER)
        waitUntil("P2 has no active turn timer") {
            !hasContentDescription(P2_ACTIVE_TURN_TIMER)
        }

        composeRule.onNodeWithContentDescription(P1_ACTIVE_TURN_TIMER, useUnmergedTree = true)
            .performClick()

        waitForContentDescription(P2_ACTIVE_TURN_TIMER)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetSamePlayersCanChooseNewTurnTimerFirstPlayer() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        enableTurnTimerAndSelectP1()
        waitForContentDescription(P1_ACTIVE_TURN_TIMER)

        openMiddleMenuItem(OPEN_RESET_GAME)
        waitForContentDescription(RESET_SAME_PLAYERS)
        composeRule.onNodeWithContentDescription(RESET_SAME_PLAYERS, useUnmergedTree = true)
            .performClick()
        waitForContentDescription(RESET_SELECT_FIRST_PLAYER)
        composeRule.onNodeWithContentDescription(RESET_SELECT_FIRST_PLAYER, useUnmergedTree = true)
            .performClick()

        waitForContentDescription(START_LIFE_COUNTER)
        composeRule.onNodeWithContentDescription(START_LIFE_COUNTER, useUnmergedTree = true)
            .performClick()

        waitForContentDescription(SELECT_P2_AS_FIRST_PLAYER)
        composeRule.onNodeWithContentDescription(SELECT_P2_AS_FIRST_PLAYER, useUnmergedTree = true)
            .performClick()

        waitForContentDescription(P2_ACTIVE_TURN_TIMER)
        waitUntil("P1 has no active turn timer after reset first-player selection") {
            !hasContentDescription(P1_ACTIVE_TURN_TIMER)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetDifferentPlayersSkipFirstPlayerClearsCustomizations() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextInput(P1_CUSTOM_NAME)
        closeCustomizationAndReturnToCounter()
        waitForText(P1_CUSTOM_NAME)

        val resetLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)
        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, resetLife + 1)

        openMiddleMenuItem(OPEN_RESET_GAME)
        performSemanticClick(RESET_DIFFERENT_PLAYERS)
        performSemanticClick(RESET_SKIP_FIRST_PLAYER)

        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, resetLife)
        waitUntil("P1 custom name removed after different-player reset") {
            !hasText(P1_CUSTOM_NAME)
        }
        waitForText("P1")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun playerSelectSkipReturnsToExistingGame() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        val initialLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)
        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife + 1)

        openMiddleMenuItem(OPEN_PLAYER_SELECT)
        performSemanticClick(START_LIFE_COUNTER)

        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife + 1)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun playerSelectTwoFingerSelectionPreservesExistingRoster() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLAYER_SELECT)
        performTwoFingerPlayerSelect()

        waitForContentDescription(P2_SETTINGS_BUTTON)
        waitForContentDescription(P3_SETTINGS_BUTTON)
        waitForContentDescription(P4_SETTINGS_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetDifferentPlayersSelectCanStartTwoPlayerGame() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_RESET_GAME)
        performSemanticClick(RESET_DIFFERENT_PLAYERS)
        performSemanticClick(RESET_SELECT_FIRST_PLAYER)

        waitForContentDescription(START_LIFE_COUNTER)
        performTwoFingerPlayerSelect()

        waitForContentDescription(P2_SETTINGS_BUTTON)
        waitUntil("P3 removed after two-player reset selection") {
            !hasContentDescription(P3_SETTINGS_BUTTON)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chanceDialogsCanRollDiceAndFlipCoin() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_DICE_ROLL)
        waitForContentDescription(ROLL_D6)
        composeRule.onNodeWithContentDescription(ROLL_D6, useUnmergedTree = true)
            .performTouchInput { click() }
        waitUntil("last dice result") {
            findIntContentDescription(LAST_DICE_RESULT_PREFIX) != null
        }
        closeDialogAndWaitForCounter()

        openMiddleMenuItem(OPEN_COIN_FLIP)
        waitForIntContentDescription(COINS_TO_FLIP_PREFIX, 1)
        composeRule.onNodeWithContentDescription(DECREASE_COINS_TO_FLIP, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(COINS_TO_FLIP_PREFIX, 1)
        composeRule.onNodeWithContentDescription(INCREASE_COINS_TO_FLIP, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(COINS_TO_FLIP_PREFIX, 2)
        composeRule.onNodeWithContentDescription(DECREASE_COINS_TO_FLIP, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(COINS_TO_FLIP_PREFIX, 1)

        waitForIntContentDescription(KRARKS_THUMBS_PREFIX, 0)
        composeRule.onNodeWithContentDescription(INCREASE_KRARKS_THUMBS, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(KRARKS_THUMBS_PREFIX, 1)
        composeRule.onNodeWithContentDescription(DECREASE_KRARKS_THUMBS, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(KRARKS_THUMBS_PREFIX, 0)

        composeRule.onAllNodesWithContentDescription(FLIP_COIN, useUnmergedTree = true)[0]
            .performTouchInput { click() }
        waitUntil("coin flip last result") {
            findContentDescriptionValue(COIN_FLIP_LAST_RESULT_PREFIX).orEmpty().isNotBlank()
        }

        closeDialogAndWaitForCounter()
        waitForContentDescription(P1_SETTINGS_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun planechaseCanSelectPlaneAndPlaneswalk() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLANECHASE)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)

        selectE2EPlaneDeck()
        waitForIntContentDescription(PLANAR_BACK_STACK_SIZE_PREFIX, 0)

        performSemanticClick(PLANESWALK)
        waitForIntContentDescription(PLANAR_BACK_STACK_SIZE_PREFIX, 1)

        performSemanticClick(PREVIOUS_PLANE)
        waitForIntContentDescription(PLANAR_BACK_STACK_SIZE_PREFIX, 0)

        closeDialogAndWaitForCounter()
        waitForContentDescription(P1_SETTINGS_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun planechasePlanarDieShowsResult() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLANECHASE)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)

        selectE2EPlaneDeck()

        performSemanticClick(ROLL_PLANAR_DIE)

        waitUntil("planar die result") {
            findContentDescriptionValue(PLANAR_DIE_RESULT_PREFIX) in PLANAR_DIE_RESULTS
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun planechasePlaneswalkDieResultMovesPlaneToBackStack() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLANECHASE)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)
        selectE2EPlaneDeck()
        waitForIntContentDescription(PLANAR_BACK_STACK_SIZE_PREFIX, 0)

        performSemanticClick(ROLL_PLANAR_DIE)
        waitForContentDescription("$PLANAR_DIE_RESULT_PREFIX$PLANESWALK")
        composeRule.onNodeWithContentDescription("$PLANAR_DIE_RESULT_PREFIX$PLANESWALK", useUnmergedTree = true)
            .performTouchInput { click() }

        waitForIntContentDescription(PLANAR_BACK_STACK_SIZE_PREFIX, 1)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun planechaseSearchBackRestoresFullPlaneListBeforeClosingDeck() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLANECHASE)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)

        performSemanticClick(OPEN_PLANAR_DECK)
        waitForContentDescription(NO_PLANES_SELECTED)

        composeRule.onNodeWithContentDescription(PLANAR_SEARCH_FIELD, useUnmergedTree = true)
            .performTextInput(EMPTY_PLANE_SEARCH_QUERY)
        composeRule.onNodeWithContentDescription(PLANAR_SEARCH_FIELD, useUnmergedTree = true)
            .performImeAction()
        waitForContentDescription(NO_VISIBLE_PLANES_SELECTED)

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(NO_PLANES_SELECTED)

        performSemanticClick(BACK_IN_DIALOG)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun planechaseDeckControlsCanHideShowAndUnselectPlanes() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLANECHASE)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)

        performSemanticClick(OPEN_PLANAR_DECK)
        waitForContentDescription(NO_PLANES_SELECTED)

        performSemanticClick(HIDE_UNSELECTED_PLANES)
        waitForContentDescription(NO_VISIBLE_PLANES_SELECTED)
        performSemanticClick(SHOW_UNSELECTED_PLANES)
        waitForContentDescription(NO_PLANES_SELECTED)

        performSemanticClick(SELECT_ALL_PLANES)
        waitForContentDescription(ONE_PLANE_SELECTED)
        performSemanticClick(UNSELECT_ALL_PLANES)
        waitForContentDescription(NO_PLANES_SELECTED)

        performSemanticClick(DONE_SELECTING_PLANES)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleMenuCanChangePlayerCountResetGameAndResetTableCounters() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLAYER_NUMBER)
        waitForContentDescription(SET_PLAYER_COUNT_2)
        composeRule.onNodeWithContentDescription(SET_PLAYER_COUNT_2, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P2_SETTINGS_BUTTON)
        waitUntil("P3 removed after setting player count to 2") {
            !hasContentDescription(P3_SETTINGS_BUTTON)
        }

        val resetLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)
        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, resetLife + 1)

        openMiddleMenuItem(OPEN_RESET_GAME)
        waitForContentDescription(RESET_SAME_PLAYERS)
        composeRule.onNodeWithContentDescription(RESET_SAME_PLAYERS, useUnmergedTree = true)
            .performClick()
        waitForContentDescription(RESET_SKIP_FIRST_PLAYER)
        composeRule.onNodeWithContentDescription(RESET_SKIP_FIRST_PLAYER, useUnmergedTree = true)
            .performClick()
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, resetLife)

        openMiddleMenuItem(OPEN_TABLE_COUNTERS)
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 0)
        composeRule.onNodeWithContentDescription(INCREASE_WHITE_MANA, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 1)

        composeRule.onNodeWithContentDescription(DECREASE_WHITE_MANA, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 0)
        composeRule.onNodeWithContentDescription(DECREASE_WHITE_MANA, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 0)

        composeRule.onNodeWithContentDescription(INCREASE_WHITE_MANA, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 1)
        composeRule.onNodeWithContentDescription(RESET_TABLE_COUNTERS, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(WHITE_MANA_COUNTER_PREFIX, 0)

        composeRule.onNodeWithContentDescription(CLOSE_DIALOG, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_SETTINGS_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun playerNumberCanSelectFourPlayerLayouts() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openMiddleMenuItem(OPEN_PLAYER_NUMBER)
        performSemanticClick(CHOOSE_4_PLAYER_LAYOUT)
        performSemanticClick(SET_ALTERNATE_4_PLAYER_LAYOUT)
        waitForContentDescription(FOUR_PLAYER_ALTERNATE_LAYOUT)

        openMiddleMenuItem(OPEN_PLAYER_NUMBER)
        performSemanticClick(CHOOSE_4_PLAYER_LAYOUT)
        performSemanticClick(SET_DEFAULT_4_PLAYER_LAYOUT)
        waitForContentDescription(FOUR_PLAYER_DEFAULT_LAYOUT)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleMenuCanSetStartingLife() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openStartingLifeDialog()
        composeRule.onNodeWithContentDescription(SET_STARTING_LIFE_20, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, 20)

        openStartingLifeDialog()
        composeRule.onNodeWithContentDescription(SET_STARTING_LIFE_40, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, 40)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun middleMenuCanSetCustomStartingLife() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openStartingLifeDialog()
        composeRule.onNodeWithContentDescription(CUSTOM_STARTING_LIFE_INPUT, useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithContentDescription(CUSTOM_STARTING_LIFE_INPUT, useUnmergedTree = true)
            .performTextInput("37")
        composeRule.onNodeWithContentDescription(CUSTOM_STARTING_LIFE_INPUT, useUnmergedTree = true)
            .performImeAction()

        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, 37)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationNamePersistsAfterLifeChange() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()

        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextInput(P1_CUSTOM_NAME)

        composeRule.onNodeWithContentDescription(CLOSE_DIALOG, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(P1_BACK_BUTTON)
        composeRule.onNodeWithContentDescription(P1_BACK_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForText(P1_CUSTOM_NAME)
        val initialLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)

        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife + 1)
        waitForText(P1_CUSTOM_NAME)

        composeRule.onNodeWithContentDescription(P1_DECREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife)
        waitForText(P1_CUSTOM_NAME)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationBackgroundColorPersistsAfterLifeChange() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        changeP1CustomizationColorAndVerifyAfterLifeChange(
            openColorPicker = CHANGE_BACKGROUND_COLOR,
            selectColor = SELECT_TEST_BACKGROUND_COLOR,
            expectedStateDescription = "$P1_BACKGROUND_COLOR_PREFIX$TEST_BACKGROUND_COLOR_ARGB"
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationTextColorPersistsAfterLifeChange() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        changeP1CustomizationColorAndVerifyAfterLifeChange(
            openColorPicker = CHANGE_TEXT_COLOR,
            selectColor = SELECT_TEST_TEXT_COLOR,
            expectedStateDescription = "$P1_TEXT_COLOR_PREFIX$TEST_TEXT_COLOR_ARGB"
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun customizationColorPickerBackReturnsToCustomizationThenCloses() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        performSemanticClick(CHANGE_BACKGROUND_COLOR)
        waitForContentDescription(SELECT_TEST_BACKGROUND_COLOR)

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)

        performSemanticClick(BACK_IN_DIALOG)
        waitForContentDescription(MIDDLE_MENU_BUTTON)
    }

    private fun changeP1CustomizationColorAndVerifyAfterLifeChange(
        openColorPicker: String,
        selectColor: String,
        expectedStateDescription: String,
    ) {
        openP1Customization()
        performSemanticClick(openColorPicker)
        performSemanticClick(selectColor)

        closeCustomizationAndReturnToCounter()
        verifyP1StatePersistsAfterLifeChange(expectedStateDescription)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun savedProfileCanBeLoadedForAnotherPlayer() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        openP1Customization()
        waitForContentDescription(P1_CUSTOMIZATION_NAME_FIELD)
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZATION_NAME_FIELD, useUnmergedTree = true)
            .performTextInput(SAVED_PROFILE_NAME)
        performSemanticClick(CHANGE_BACKGROUND_COLOR)
        performSemanticClick(SELECT_TEST_BACKGROUND_COLOR)
        closeCustomizationAndReturnToCounter()

        openP2Customization()
        performSemanticClick(OPEN_LOAD_PROFILE)
        performSemanticClick("$LOAD_PROFILE_PREFIX$SAVED_PROFILE_NAME")
        closeCustomizationAndReturnToCounter(settingsButton = P2_SETTINGS_BUTTON, backButton = P2_BACK_BUTTON)

        waitForText(SAVED_PROFILE_NAME)
        waitForContentDescription("$P2_BACKGROUND_COLOR_PREFIX$TEST_BACKGROUND_COLOR_ARGB")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun playerSettingsCanToggleMonarchAndChangeCounters() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_SETTINGS_BUTTON)
        composeRule.onNodeWithContentDescription(P1_SETTINGS_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        if (hasContentDescription(P1_CLEAR_MONARCH)) {
            composeRule.onNodeWithContentDescription(P1_CLEAR_MONARCH, useUnmergedTree = true)
                .performTouchInput { click() }
            waitForContentDescription(P1_BECOME_MONARCH)
        }

        waitForContentDescription(P1_BECOME_MONARCH)
        composeRule.onNodeWithContentDescription(P1_BECOME_MONARCH, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_CLEAR_MONARCH)

        composeRule.onNodeWithContentDescription(P1_CLEAR_MONARCH, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_BECOME_MONARCH)

        composeRule.onNodeWithContentDescription(P1_OPEN_COUNTERS, useUnmergedTree = true)
            .performTouchInput { click() }

        if (readFirstP1Counter() == null) {
            waitForContentDescription(P1_ADD_COUNTER)
            composeRule.onNodeWithContentDescription(P1_ADD_COUNTER, useUnmergedTree = true)
                .performTouchInput { click() }
            waitForContentDescription(P1_ADD_POISON_COUNTER)
            composeRule.onNodeWithContentDescription(P1_ADD_POISON_COUNTER, useUnmergedTree = true)
                .performTouchInput { click() }
            composeRule.onNodeWithContentDescription(P1_BACK_BUTTON, useUnmergedTree = true)
                .performTouchInput { click() }
            waitForIntContentDescription(P1_POISON_COUNTER_PREFIX, 0)
        }

        val initialCounter = readFirstP1Counter() ?: error("No P1 counter available")

        composeRule.onNodeWithContentDescription("P1 increase ${initialCounter.name} counter", useUnmergedTree = true)
            .performTouchInput { click() }
        waitForCounterValue(initialCounter.name, initialCounter.value + 1)

        composeRule.onNodeWithContentDescription("P1 decrease ${initialCounter.name} counter", useUnmergedTree = true)
            .performTouchInput { click() }
        waitForCounterValue(initialCounter.name, initialCounter.value)

        composeRule.onNodeWithContentDescription(P1_BACK_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_BECOME_MONARCH)

        composeRule.onNodeWithContentDescription(P1_BACK_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_SETTINGS_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun playerKoHidesLifeControlsUntilGameReset() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_ALIVE)
        performSemanticClick(P1_SETTINGS_BUTTON)
        performSemanticClick(KO_P1)

        waitForContentDescription(P1_DEAD)
        waitUntil("P1 life controls hidden while dead") {
            !hasContentDescription(P1_INCREASE_LIFE) &&
                !hasContentDescription(P1_DECREASE_LIFE)
        }

        openMiddleMenuItem(OPEN_RESET_GAME)
        waitForContentDescription(RESET_SAME_PLAYERS)
        composeRule.onNodeWithContentDescription(RESET_SAME_PLAYERS, useUnmergedTree = true)
            .performClick()
        waitForContentDescription(RESET_SKIP_FIRST_PLAYER)
        composeRule.onNodeWithContentDescription(RESET_SKIP_FIRST_PLAYER, useUnmergedTree = true)
            .performClick()

        waitForContentDescription(P1_ALIVE)
        waitForContentDescription(P1_INCREASE_LIFE)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lifeTotalCanIncrementAndDecrement() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        val initialLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)

        waitForContentDescription(P1_INCREASE_LIFE)
        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife + 1)

        composeRule.onNodeWithContentDescription(P1_DECREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commanderDamageCanIncrementAndDecrement() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_COMMANDER_BUTTON)
        composeRule.onNodeWithContentDescription(P1_COMMANDER_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        val initialPrimaryDamage = readIntContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_PREFIX)

        waitForContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_INCREASE)
        composeRule.onNodeWithContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_INCREASE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_PREFIX, initialPrimaryDamage + 1)

        composeRule.onNodeWithContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_DECREASE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P2_PRIMARY_COMMANDER_DAMAGE_PREFIX, initialPrimaryDamage)

        composeRule.onNodeWithContentDescription(ENABLE_PARTNER_COMMANDER_DAMAGE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(DISABLE_PARTNER_COMMANDER_DAMAGE)

        val initialPartnerDamage = readIntContentDescription(P2_PARTNER_COMMANDER_DAMAGE_PREFIX)

        waitForContentDescription(P2_PARTNER_COMMANDER_DAMAGE_INCREASE)
        composeRule.onNodeWithContentDescription(P2_PARTNER_COMMANDER_DAMAGE_INCREASE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P2_PARTNER_COMMANDER_DAMAGE_PREFIX, initialPartnerDamage + 1)

        composeRule.onNodeWithContentDescription(P2_PARTNER_COMMANDER_DAMAGE_DECREASE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P2_PARTNER_COMMANDER_DAMAGE_PREFIX, initialPartnerDamage)

        composeRule.onNodeWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_COMMANDER_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commanderModeCanEnterAndExit() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_COMMANDER_BUTTON)
        composeRule.onNodeWithContentDescription(P1_COMMANDER_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(COMMANDER_EXIT_BUTTON)
        waitForContentDescription(P1_COMMANDER_DEALER)

        composeRule.onNodeWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(MIDDLE_MENU_BUTTON)
        waitForContentDescription(P1_COMMANDER_BUTTON)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commanderPartnerModeCanToggleOnAndOff() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_COMMANDER_BUTTON)
        composeRule.onNodeWithContentDescription(P1_COMMANDER_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(ENABLE_PARTNER_COMMANDER_DAMAGE)
        composeRule.onNodeWithContentDescription(ENABLE_PARTNER_COMMANDER_DAMAGE, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(DISABLE_PARTNER_COMMANDER_DAMAGE)
        composeRule.onNodeWithContentDescription(DISABLE_PARTNER_COMMANDER_DAMAGE, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(ENABLE_PARTNER_COMMANDER_DAMAGE)
        composeRule.onNodeWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(P1_COMMANDER_BUTTON)
    }

    private fun exitCommanderModeIfNeeded() {
        if (composeRule.onAllNodesWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()) {
            performSemanticClick(COMMANDER_EXIT_BUTTON)
        }
    }

    private fun openLifeCounterIfNeeded() {
        waitUntil("life counter or splash action") {
            hasContentDescription(MIDDLE_MENU_BUTTON) ||
                hasContentDescription(COMMANDER_EXIT_BUTTON) ||
                hasContentDescription(START_LIFE_COUNTER)
        }

        if (hasContentDescription(START_LIFE_COUNTER) && !isRealLifeCounterVisible()) {
            performSemanticClick(START_LIFE_COUNTER)
            waitForRealLifeCounter()
        }

        waitForRealLifeCounter()
    }

    private fun openStartingLifeDialog() {
        openMiddleMenuItem(OPEN_STARTING_LIFE)
        waitForContentDescription(SET_STARTING_LIFE_40)
    }

    private fun searchScryfallForTestCard() {
        waitForContentDescription(SCRYFALL_SEARCH_FIELD)
        composeRule.onNodeWithContentDescription(SCRYFALL_SEARCH_FIELD, useUnmergedTree = true)
            .performTextInput(SCRYFALL_TEST_QUERY)
        composeRule.onNodeWithContentDescription(SEARCH_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForText(TEST_CARD_NAME)
    }

    private fun searchGifForTestResult() {
        waitForContentDescription(GIF_SEARCH_FIELD)
        composeRule.onNodeWithContentDescription(GIF_SEARCH_FIELD, useUnmergedTree = true)
            .performTextInput(GIF_TEST_QUERY)
        composeRule.onNodeWithContentDescription(SEARCH_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForContentDescription(TEST_GIF_RESULT)
    }

    private fun openMiddleMenuItem(contentDescription: String) {
        openMiddleMenu()
        performSemanticClick(contentDescription)
    }

    private fun openMiddleMenu() {
        waitForContentDescription(MIDDLE_MENU_BUTTON)
        composeRule.onNodeWithContentDescription(MIDDLE_MENU_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }
    }

    private fun enableTurnTimerAndSelectP1() {
        openMiddleMenu()
        performSemanticClick(OPEN_APP_SETTINGS)
        val initialTurnTimer = readContentDescriptionValue(TURN_TIMER_SETTING_PREFIX)
        performClickOnContentDescriptionPrefix(TURN_TIMER_SETTING_PREFIX)
        waitUntil("turn timer changed") {
            findContentDescriptionValue(TURN_TIMER_SETTING_PREFIX)
                ?.let { it != initialTurnTimer } == true
        }

        performSemanticClick(CLOSE_DIALOG)
        waitUntil("settings dialog closed") {
            !hasContentDescription(CLOSE_DIALOG)
        }

        waitForContentDescription(SELECT_P1_AS_FIRST_PLAYER)
        composeRule.onNodeWithContentDescription(SELECT_P1_AS_FIRST_PLAYER, useUnmergedTree = true)
            .performClick()
    }

    private fun openP1Customization() {
        openCustomization(P1_SETTINGS_BUTTON, P1_CUSTOMIZE)
    }

    private fun openP2Customization() {
        openCustomization(P2_SETTINGS_BUTTON, P2_CUSTOMIZE)
    }

    private fun openCustomization(settingsButton: String, customizeButton: String) {
        waitForContentDescription(settingsButton)
        composeRule.onNodeWithContentDescription(settingsButton, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(customizeButton)
        composeRule.onNodeWithContentDescription(customizeButton, useUnmergedTree = true)
            .performTouchInput { click() }
    }

    private fun closeCustomizationAndReturnToCounter(
        settingsButton: String = P1_SETTINGS_BUTTON,
        backButton: String = P1_BACK_BUTTON,
    ) {
        composeRule.onNodeWithContentDescription(CLOSE_DIALOG, useUnmergedTree = true)
            .performTouchInput { click() }
        waitUntil("customization closed") {
            !hasContentDescription(CLOSE_DIALOG) &&
                (hasContentDescription(backButton) || hasContentDescription(settingsButton))
        }
        if (hasContentDescription(backButton)) {
            composeRule.onNodeWithContentDescription(backButton, useUnmergedTree = true)
                .performTouchInput { click() }
        }
        waitForContentDescription(settingsButton)
    }

    private fun closeDialogAndWaitForCounter() {
        composeRule.onNodeWithContentDescription(CLOSE_DIALOG, useUnmergedTree = true)
            .performTouchInput { click() }
        waitUntil("dialog closed") {
            !hasContentDescription(CLOSE_DIALOG) && hasContentDescription(MIDDLE_MENU_BUTTON)
        }
    }

    private fun verifyP1StatePersistsAfterLifeChange(expectedStateDescription: String) {
        waitForContentDescription(expectedStateDescription)
        val initialLife = readIntContentDescription(P1_LIFE_TOTAL_PREFIX)
        composeRule.onNodeWithContentDescription(P1_INCREASE_LIFE, useUnmergedTree = true)
            .performTouchInput { click() }
        waitForIntContentDescription(P1_LIFE_TOTAL_PREFIX, initialLife + 1)
        waitForContentDescription(expectedStateDescription)
    }

    private fun waitForContentDescription(value: String) {
        waitUntil(value) {
            hasContentDescription(value)
        }
    }

    private fun waitForRealLifeCounter() {
        waitUntil("real life counter") {
            isRealLifeCounterVisible()
        }
    }

    private fun waitForText(value: String) {
        waitUntil(value) {
            hasText(value)
        }
    }

    private fun readIntContentDescription(prefix: String): Int {
        var value: Int? = null
        waitUntil(prefix) {
            value = findIntContentDescription(prefix)
            value != null
        }
        return value ?: error("No content description found for $prefix")
    }

    private fun readContentDescriptionValue(prefix: String): String {
        var value: String? = null
        waitUntil(prefix) {
            value = findContentDescriptionValue(prefix)
            value != null
        }
        return value ?: error("No content description found for $prefix")
    }

    private fun waitForIntContentDescription(prefix: String, expected: Int) {
        waitUntil("$prefix$expected") {
            findIntContentDescription(prefix) == expected
        }
    }

    private fun waitForCounterValue(name: String, expected: Int) {
        waitForIntContentDescription("P1 $name counter ", expected)
    }

    private fun performClickOnContentDescriptionPrefix(prefix: String) {
        waitUntil(prefix) {
            composeRule.onAllNodes(hasContentDescriptionStartingWith(prefix), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onAllNodes(hasContentDescriptionStartingWith(prefix), useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun verifySettingToggleChanges(prefix: String) {
        val initial = readContentDescriptionValue(prefix)
        performClickOnContentDescriptionPrefix(prefix)
        waitUntil("$prefix changed") {
            findContentDescriptionValue(prefix)?.let { it != initial } == true
        }
    }

    private fun performSemanticClick(contentDescription: String) {
        waitForContentDescription(contentDescription)
        composeRule.onNodeWithContentDescription(contentDescription, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun performTwoFingerPlayerSelect() {
        composeRule.onRoot(useUnmergedTree = true)
            .performTouchInput {
                down(0, Offset(center.x - 160f, center.y))
                down(1, Offset(center.x + 160f, center.y))
                advanceEventTime(4_000)
                up(0)
                advanceEventTime(100)
                up(1)
            }
    }

    private fun selectE2EPlaneDeck() {
        performSemanticClick(OPEN_PLANAR_DECK)
        waitForContentDescription(NO_PLANES_SELECTED)
        waitForContentDescription(TEST_PLANE_SELECTION)
        performSemanticClick(SELECT_ALL_PLANES)
        waitForContentDescription(ONE_PLANE_SELECTED)
        performSemanticClick(DONE_SELECTING_PLANES)
        waitForIntContentDescription(PLANAR_DECK_SIZE_PREFIX, 1)
        waitForContentDescription("$CURRENT_PLANE_PREFIX$TEST_PLANE_NAME")
    }

    private fun readFirstP1Counter(): CounterReading? {
        return contentDescriptions().firstNotNullOfOrNull { description ->
            val counterText = description.removePrefix(P1_COUNTER_PREFIX)
            if (counterText == description) return@firstNotNullOfOrNull null

            val name = counterText.substringBefore(COUNTER_VALUE_SEPARATOR, missingDelimiterValue = "")
            if (name.isEmpty()) return@firstNotNullOfOrNull null

            val value = counterText.substringAfter(COUNTER_VALUE_SEPARATOR, missingDelimiterValue = "").toIntOrNull()
            value?.let { CounterReading(name = name, value = it) }
        }
    }

    private fun waitUntil(description: String, condition: () -> Boolean) {
        try {
            composeRule.waitUntil(timeoutMillis = 5_000, condition = condition)
        } catch (error: Throwable) {
            throw AssertionError(
                "Timed out waiting for $description. Current semantics:\n" +
                    semanticsSnapshot(),
                error
            )
        }
    }

    private fun semanticsSnapshot(): String {
        return runCatching {
            composeRule.onAllNodes(isRoot(), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .indices
                .joinToString(separator = "\n\n") { index ->
                    "Root $index:\n" +
                        composeRule.onAllNodes(isRoot(), useUnmergedTree = true)[index]
                            .printToString(maxDepth = 10)
                }
        }.getOrElse { error ->
            "Could not print semantics tree: ${error.message}"
        }
    }

    private fun hasContentDescription(value: String): Boolean {
        return composeRule.onAllNodesWithContentDescription(value, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    private fun hasText(value: String): Boolean {
        return composeRule.onAllNodesWithText(value, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    private fun isRealLifeCounterVisible(): Boolean {
        return hasContentDescription(MIDDLE_MENU_BUTTON) || hasContentDescription(COMMANDER_EXIT_BUTTON)
    }

    private fun findIntContentDescription(prefix: String): Int? {
        return contentDescriptions()
            .firstNotNullOfOrNull { description ->
                description.takeIf { it.startsWith(prefix) }
                    ?.removePrefix(prefix)
                    ?.toIntOrNull()
            }
    }

    private fun findContentDescriptionValue(prefix: String): String? {
        return contentDescriptions()
            .firstNotNullOfOrNull { description ->
                description.takeIf { it.startsWith(prefix) }
                    ?.removePrefix(prefix)
            }
    }

    private fun contentDescriptions(): List<String> {
        return composeRule.onAllNodes(HasContentDescription, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node ->
                runCatching { node.config[SemanticsProperties.ContentDescription] }
                    .getOrNull()
                    .orEmpty()
            }
    }

    private companion object {
        val HasContentDescription = SemanticsMatcher("has content description") { node ->
            runCatching { node.config[SemanticsProperties.ContentDescription] }.isSuccess
        }

        fun hasContentDescriptionStartingWith(prefix: String) = SemanticsMatcher("has content description starting with $prefix") { node ->
            runCatching { node.config[SemanticsProperties.ContentDescription] }
                .getOrNull()
                .orEmpty()
                .any { it.startsWith(prefix) }
        }

        const val P1_COMMANDER_BUTTON = "P1 commander mode"
        const val P1_COMMANDER_DEALER = "P1 is commander dealer"
        const val P1_SETTINGS_BUTTON = "P1 settings"
        const val P2_SETTINGS_BUTTON = "P2 settings"
        const val P3_SETTINGS_BUTTON = "P3 settings"
        const val P4_SETTINGS_BUTTON = "P4 settings"
        const val P1_BACK_BUTTON = "P1 back"
        const val P2_BACK_BUTTON = "P2 back"
        const val COMMANDER_EXIT_BUTTON = "Exit commander mode"
        const val MIDDLE_MENU_BUTTON = "Open life counter menu"
        const val START_LIFE_COUNTER = "Go to Life Counter"
        const val OPEN_TUTORIAL_FROM_SETTINGS = "View Tutorial Again"
        const val SKIP_TUTORIAL = "Skip tutorial"
        const val TUTORIAL_GO_FORWARD = "Go forward"
        const val TUTORIAL_GO_BACK = "Go back"
        const val CONFIRM_SKIP_TUTORIAL = "Skip"
        const val ENABLE_PARTNER_COMMANDER_DAMAGE = "Enable partner commander damage"
        const val DISABLE_PARTNER_COMMANDER_DAMAGE = "Disable partner commander damage"
        const val TOGGLE_PARTNER_MODE_LABEL = "Toggle Partner Mode"
        const val P1_LIFE_TOTAL_PREFIX = "P1 life total "
        const val P1_INCREASE_LIFE = "P1 increase life"
        const val P1_DECREASE_LIFE = "P1 decrease life"
        const val P2_PRIMARY_COMMANDER_DAMAGE_PREFIX = "P2 primary commander damage "
        const val P2_PRIMARY_COMMANDER_DAMAGE_INCREASE = "P2 primary commander damage increase"
        const val P2_PRIMARY_COMMANDER_DAMAGE_DECREASE = "P2 primary commander damage decrease"
        const val P2_PARTNER_COMMANDER_DAMAGE_PREFIX = "P2 partner commander damage "
        const val P2_PARTNER_COMMANDER_DAMAGE_INCREASE = "P2 partner commander damage increase"
        const val P2_PARTNER_COMMANDER_DAMAGE_DECREASE = "P2 partner commander damage decrease"
        const val P1_BECOME_MONARCH = "Make P1 the monarch"
        const val P1_CLEAR_MONARCH = "Clear P1 as monarch"
        const val P1_OPEN_COUNTERS = "Open P1 counters"
        const val KO_P1 = "KO P1"
        const val P1_ALIVE = "P1 death state alive"
        const val P1_DEAD = "P1 death state dead"
        const val P1_CUSTOMIZE = "Customize P1"
        const val P2_CUSTOMIZE = "Customize P2"
        const val P1_CUSTOMIZATION_NAME_FIELD = "P1 customization name"
        const val P1_CUSTOM_NAME = "P1 E2E"
        const val SAVED_PROFILE_NAME = "Saved E2E"
        const val OPEN_LOAD_PROFILE = "Open load profile"
        const val LOAD_PROFILE_PREFIX = "Load profile "
        const val DELETE_PROFILE_WARNING = "This will delete the player profile. Proceed?"
        const val CHANGE_BACKGROUND_COLOR = "Change background color"
        const val CHANGE_TEXT_COLOR = "Change text color"
        const val SELECT_COLOR_PREFIX = "Select color "
        const val P1_BACKGROUND_COLOR_PREFIX = "P1 background color "
        const val P1_BACKGROUND_IMAGE_PREFIX = "P1 background image "
        const val P1_TEXT_COLOR_PREFIX = "P1 text color "
        const val P2_BACKGROUND_COLOR_PREFIX = "P2 background color "
        val TEST_BACKGROUND_COLOR_ARGB = PlayerColor6.toArgb()
        val TEST_TEXT_COLOR_ARGB = PlayerColor7.toArgb()
        val SELECT_TEST_BACKGROUND_COLOR = "$SELECT_COLOR_PREFIX$TEST_BACKGROUND_COLOR_ARGB option 9"
        val SELECT_TEST_TEXT_COLOR = "$SELECT_COLOR_PREFIX$TEST_TEXT_COLOR_ARGB option 10"
        const val CLOSE_DIALOG = "Close dialog"
        const val UPLOAD_PLAYER_IMAGE = "Upload player image"
        const val CAMERA_ROLL_WARNING = "This will open the camera roll. Proceed?"
        const val CANCEL_WARNING = "Cancel"
        const val OPEN_CARD_IMAGE_SEARCH = "Open card image search"
        const val SCRYFALL_TEST_QUERY = "sol ring"
        const val SCRYFALL_SEARCH_FIELD = "Search Scryfall input"
        const val TEST_CARD_NAME = "E2E Card"
        const val TEST_CARD_SELECT = "Select E2E Card"
        const val TEST_CARD_IMAGE_URI = "https://example.test/e2e-card-art.jpg"
        const val TEST_PRINTING_CARD_NAME = "E2E Printing"
        const val TEST_RULING_COMMENT = "E2E ruling text"
        const val SHOW_TEST_CARD_RULINGS = "Show rulings for E2E Card"
        const val SHOW_TEST_CARD_PRINTINGS = "Show printings for E2E Card"
        const val OPEN_GIF_SEARCH = "Open GIF search"
        const val GIF_SEARCH_FIELD = "Search KLIPY input"
        const val GIF_TEST_QUERY = "cat"
        const val SEARCH_BUTTON = "Search"
        const val TEST_GIF_RESULT = "GIF result e2e-gif"
        const val TEST_GIF_IMAGE_URI = "https://example.test/e2e-full.gif"
        const val OPEN_CARD_SEARCH = "Open card search"
        const val OPEN_PLAYER_SELECT = "Open player select"
        const val OPEN_RESET_GAME = "Open reset game"
        const val RESET_SAME_PLAYERS = "Same players"
        const val RESET_DIFFERENT_PLAYERS = "Different players"
        const val RESET_SELECT_FIRST_PLAYER = "Select"
        const val RESET_SKIP_FIRST_PLAYER = "Skip"
        const val BACK_IN_DIALOG = "Back in dialog"
        const val TOGGLE_THEME = "Toggle theme"
        const val THEME_STATE_PREFIX = "Theme "
        const val TOGGLE_DAY_NIGHT = "Toggle day night"
        const val DAY_NIGHT_STATE_PREFIX = "Day night state "
        const val OPEN_APP_SETTINGS = "Open app settings"
        const val OPEN_PATCH_NOTES = "Patch Notes"
        const val CHANGE_LOG_TITLE = "Change Log"
        const val FAST_COIN_FLIP_SETTING_PREFIX = "Fast Coin Flip setting "
        const val DISABLE_CAMERA_ROLL_SETTING_PREFIX = "Disable Camera Roll setting "
        const val AUTO_KO_SETTING_PREFIX = "Auto KO setting "
        const val AUTO_SKIP_PLAYER_SELECT_SETTING_PREFIX = "Auto Skip Player Select setting "
        const val KEEP_SCREEN_ON_SETTING_PREFIX = "Keep Screen On setting "
        const val TURN_TIMER_SETTING_PREFIX = "Turn Timer setting "
        const val SELECT_P1_AS_FIRST_PLAYER = "Select P1 as first player"
        const val SELECT_P2_AS_FIRST_PLAYER = "Select P2 as first player"
        const val P1_ACTIVE_TURN_TIMER = "P1 active turn timer"
        const val P2_ACTIVE_TURN_TIMER = "P2 active turn timer"
        const val OPEN_PLAYER_NUMBER = "Open player number"
        const val SET_PLAYER_COUNT_2 = "Set player count to 2"
        const val CHOOSE_4_PLAYER_LAYOUT = "Choose 4 player layout"
        const val SET_ALTERNATE_4_PLAYER_LAYOUT = "Set alternate 4 player layout"
        const val SET_DEFAULT_4_PLAYER_LAYOUT = "Set default 4 player layout"
        const val FOUR_PLAYER_ALTERNATE_LAYOUT = "Player layout 4 alternate"
        const val FOUR_PLAYER_DEFAULT_LAYOUT = "Player layout 4 default"
        const val OPEN_STARTING_LIFE = "Open starting life"
        const val SET_STARTING_LIFE_20 = "Set starting life to 20"
        const val SET_STARTING_LIFE_40 = "Set starting life to 40"
        const val CUSTOM_STARTING_LIFE_INPUT = "Custom starting life input"
        const val OPEN_TABLE_COUNTERS = "Open mana and storm counters"
        const val WHITE_MANA_COUNTER_PREFIX = "White mana "
        const val INCREASE_WHITE_MANA = "Increase white mana"
        const val DECREASE_WHITE_MANA = "Decrease white mana"
        const val RESET_TABLE_COUNTERS = "Reset table counters"
        const val OPEN_DICE_ROLL = "Open dice roll"
        const val ROLL_D6 = "Roll D6"
        const val LAST_DICE_RESULT_PREFIX = "Last dice result "
        const val OPEN_COIN_FLIP = "Open coin flip"
        const val COINS_TO_FLIP_PREFIX = "Coins to flip "
        const val KRARKS_THUMBS_PREFIX = "Krark's thumbs "
        const val INCREASE_COINS_TO_FLIP = "Increase coins to flip"
        const val DECREASE_COINS_TO_FLIP = "Decrease coins to flip"
        const val INCREASE_KRARKS_THUMBS = "Increase Krark's thumbs"
        const val DECREASE_KRARKS_THUMBS = "Decrease Krark's thumbs"
        const val FLIP_COIN = "Flip coin"
        const val COIN_FLIP_LAST_RESULT_PREFIX = "Coin flip last result "
        const val OPEN_PLANECHASE = "Open planechase"
        const val OPEN_PLANAR_DECK = "Open planar deck"
        const val PLANAR_SEARCH_FIELD = "Search planes input"
        const val PLANAR_DECK_SIZE_PREFIX = "Planar deck size "
        const val PLANAR_BACK_STACK_SIZE_PREFIX = "Planar back stack size "
        const val TEST_PLANE_NAME = "E2E Plane"
        const val TEST_PLANE_SELECTION = "Plane selection E2E Plane not selected"
        const val NO_PLANES_SELECTED = "0 of 1 planes selected"
        const val EMPTY_PLANE_SEARCH_QUERY = "empty-plane-search"
        const val NO_VISIBLE_PLANES_SELECTED = "0 of 0 planes selected"
        const val ONE_PLANE_SELECTED = "1 of 1 planes selected"
        const val SELECT_ALL_PLANES = "Select all planes"
        const val UNSELECT_ALL_PLANES = "Unselect all planes"
        const val HIDE_UNSELECTED_PLANES = "Hide unselected planes"
        const val SHOW_UNSELECTED_PLANES = "Show unselected planes"
        const val DONE_SELECTING_PLANES = "Done selecting planes"
        const val CURRENT_PLANE_PREFIX = "Current plane "
        const val PLANESWALK = "Planeswalk"
        const val ROLL_PLANAR_DIE = "Roll planar die"
        const val PLANAR_DIE_RESULT_PREFIX = "Planar die result "
        const val PREVIOUS_PLANE = "Previous plane"
        const val P1_ADD_COUNTER = "Add P1 counter"
        const val P1_ADD_POISON_COUNTER = "Add P1 Poison counter"
        const val P1_COUNTER_PREFIX = "P1 "
        const val COUNTER_VALUE_SEPARATOR = " counter "
        const val P1_POISON_COUNTER_PREFIX = "P1 Poison counter "
        val PLANAR_DIE_RESULTS = setOf("Planeswalk", "Chaos Ensues", "No Effect")
    }

    private data class CounterReading(
        val name: String,
        val value: Int,
    )
}
