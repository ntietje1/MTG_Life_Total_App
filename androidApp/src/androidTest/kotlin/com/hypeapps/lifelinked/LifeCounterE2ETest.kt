package com.hypeapps.lifelinked

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LifeCounterE2ETest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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

    private fun exitCommanderModeIfNeeded() {
        if (composeRule.onAllNodesWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true)
                .performTouchInput { click() }
        }
    }

    private fun openLifeCounterIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            hasContentDescription(P1_COMMANDER_BUTTON) || hasContentDescription(START_LIFE_COUNTER)
        }
        if (hasContentDescription(P1_COMMANDER_BUTTON)) {
            return
        }
        composeRule.onNodeWithContentDescription(START_LIFE_COUNTER, useUnmergedTree = true).performClick()
    }

    private fun waitForContentDescription(value: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            hasContentDescription(value)
        }
    }

    private fun hasContentDescription(value: String): Boolean {
        return composeRule.onAllNodesWithContentDescription(value, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    private companion object {
        const val P1_COMMANDER_BUTTON = "P1 commander mode"
        const val P1_COMMANDER_DEALER = "P1 is commander dealer"
        const val COMMANDER_EXIT_BUTTON = "Exit commander mode"
        const val MIDDLE_MENU_BUTTON = "Open life counter menu"
        const val START_LIFE_COUNTER = "Go to Life Counter"
    }
}
