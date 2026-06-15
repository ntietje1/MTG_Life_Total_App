package com.hypeapps.lifelinked

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.printToString
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
    fun customizationNamePersistsAfterLifeChange() {
        openLifeCounterIfNeeded()
        exitCommanderModeIfNeeded()

        waitForContentDescription(P1_SETTINGS_BUTTON)
        composeRule.onNodeWithContentDescription(P1_SETTINGS_BUTTON, useUnmergedTree = true)
            .performTouchInput { click() }

        waitForContentDescription(P1_CUSTOMIZE)
        composeRule.onNodeWithContentDescription(P1_CUSTOMIZE, useUnmergedTree = true)
            .performTouchInput { click() }

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
            composeRule.onNodeWithContentDescription(COMMANDER_EXIT_BUTTON, useUnmergedTree = true)
                .performTouchInput { click() }
        }
    }

    private fun openLifeCounterIfNeeded() {
        waitUntil("life counter or splash action") {
            hasContentDescription(P1_COMMANDER_BUTTON) ||
                hasContentDescription(COMMANDER_EXIT_BUTTON) ||
                hasContentDescription(START_LIFE_COUNTER)
        }
        if (hasContentDescription(P1_COMMANDER_BUTTON) || hasContentDescription(COMMANDER_EXIT_BUTTON)) {
            return
        }
        composeRule.onNodeWithContentDescription(START_LIFE_COUNTER, useUnmergedTree = true)
            .performTouchInput { click() }
    }

    private fun waitForContentDescription(value: String) {
        waitUntil(value) {
            hasContentDescription(value)
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

    private fun waitForIntContentDescription(prefix: String, expected: Int) {
        waitUntil("$prefix$expected") {
            findIntContentDescription(prefix) == expected
        }
    }

    private fun waitForCounterValue(name: String, expected: Int) {
        waitForIntContentDescription("P1 $name counter ", expected)
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
            composeRule.onRoot(useUnmergedTree = true).printToString(maxDepth = 10)
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

    private fun findIntContentDescription(prefix: String): Int? {
        return contentDescriptions()
            .firstNotNullOfOrNull { description ->
                description.takeIf { it.startsWith(prefix) }
                    ?.removePrefix(prefix)
                    ?.toIntOrNull()
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

        const val P1_COMMANDER_BUTTON = "P1 commander mode"
        const val P1_COMMANDER_DEALER = "P1 is commander dealer"
        const val P1_SETTINGS_BUTTON = "P1 settings"
        const val P1_BACK_BUTTON = "P1 back"
        const val COMMANDER_EXIT_BUTTON = "Exit commander mode"
        const val MIDDLE_MENU_BUTTON = "Open life counter menu"
        const val START_LIFE_COUNTER = "Go to Life Counter"
        const val ENABLE_PARTNER_COMMANDER_DAMAGE = "Enable partner commander damage"
        const val DISABLE_PARTNER_COMMANDER_DAMAGE = "Disable partner commander damage"
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
        const val P1_CUSTOMIZE = "Customize P1"
        const val P1_CUSTOMIZATION_NAME_FIELD = "P1 customization name"
        const val P1_CUSTOM_NAME = "P1 E2E"
        const val CLOSE_DIALOG = "Close dialog"
        const val P1_ADD_COUNTER = "Add P1 counter"
        const val P1_ADD_POISON_COUNTER = "Add P1 Poison counter"
        const val P1_COUNTER_PREFIX = "P1 "
        const val COUNTER_VALUE_SEPARATOR = " counter "
        const val P1_POISON_COUNTER_PREFIX = "P1 Poison counter "
    }

    private data class CounterReading(
        val name: String,
        val value: Int,
    )
}
