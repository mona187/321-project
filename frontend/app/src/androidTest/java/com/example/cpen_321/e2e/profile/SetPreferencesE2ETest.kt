package com.example.cpen_321.e2e.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.cpen_321.MainActivity
import com.example.cpen_321.e2e.utils.ScreenObjects
import com.example.cpen_321.e2e.utils.TestHelpers.assertTextExists
import com.example.cpen_321.e2e.utils.TestHelpers.clickButton
import com.example.cpen_321.e2e.utils.TestHelpers.disableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.enableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.waitForSnackbar
import com.example.cpen_321.e2e.utils.TestHelpers.waitForText
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for Set Preferences Use Case
 *
 * Tests the main success scenario and failure scenarios according to:
 * Feature: Profile Creation
 * Use Case: Set Preferences
 *
 * Main success scenario:
 * 1. User is on "ProfileConfigScreen" and clicks on "Preferences" button
 * 2. User is brought into "PreferencesScreen"
 * 3. User selects 0 or more preferences
 * 4. User drags "Max amount of money to spend" scrollbar to desired amount
 * 5. User drags "Search radius" scrollbar to desired amount
 * 6. User clicks "Save Preferences" to save preferences
 * 7. The screen refreshes and "Settings updated successfully" message is printed
 *
 * Failure scenarios:
 * 7. Internet connection error
 *    7.a.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SetPreferencesE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.disableAirplaneMode()

        // Wait for home screen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Start Matchmaking", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        navigateToProfileConfig()
    }

    @After
    fun tearDown() {
        device.disableAirplaneMode()

        // Navigate home
        try {
            composeTestRule.onNodeWithContentDescription("Home").performClick()
            composeTestRule.waitForIdle()
        } catch (e: Exception) {
            // Already at home
        }
    }

    private fun navigateToProfileConfig() {
        composeTestRule.waitForText("Welcome", substring = true, timeoutMillis = 5000)

        composeTestRule.onAllNodesWithContentDescription("Profile", useUnmergedTree = true)
            .onLast()
            .performClick()

        composeTestRule.waitForText(ScreenObjects.PROFILE_BUTTON, timeoutMillis = 3000)
    }

    @Test
    fun testSetPreferences_MainSuccessScenario() {
        composeTestRule.assertTextExists(ScreenObjects.PREFERENCES_BUTTON)
        composeTestRule.clickButton(ScreenObjects.PREFERENCES_BUTTON)

        composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.PREFERENCES_TITLE)

        composeTestRule.clickButton(ScreenObjects.SUSHI)
        composeTestRule.clickButton(ScreenObjects.ITALIAN)
        composeTestRule.clickButton(ScreenObjects.PIZZA)

        composeTestRule.onNodeWithText(ScreenObjects.SUSHI, useUnmergedTree = true)
            .assertExists()

        composeTestRule.onNode(
            hasText(ScreenObjects.MAX_MONEY_PREFIX, substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 0f, range = 0f..200f)),
            useUnmergedTree = true
        ).onFirst()
            .performTouchInput {
                val width = this.width.toFloat()
                val targetX = width * 0.375f
                swipeRight(startX = 0f, endX = targetX)
            }

        composeTestRule.onNode(
            hasText(ScreenObjects.SEARCH_RADIUS_PREFIX, substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 0f, range = 1f..50f)),
            useUnmergedTree = true
        ).onFirst()
            .performTouchInput {
                val width = this.width.toFloat()
                val targetX = width * 0.5f
                swipeRight(startX = 0f, endX = targetX)
            }

        composeTestRule.clickButton(ScreenObjects.SAVE_PREFERENCES)

        composeTestRule.waitForSnackbar(ScreenObjects.SETTINGS_UPDATED, timeoutMillis = 10000)
        composeTestRule.assertTextExists(ScreenObjects.SETTINGS_UPDATED)
    }

    @Test
    fun testSetPreferences_WithZeroSelections() {
        composeTestRule.clickButton(ScreenObjects.PREFERENCES_BUTTON)
        composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE)

        composeTestRule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 0f, range = 0f..200f)),
            useUnmergedTree = true
        ).onFirst()
            .performTouchInput { swipeRight(startX = 0f, endX = width.toFloat() * 0.5f) }

        composeTestRule.clickButton(ScreenObjects.SAVE_PREFERENCES)
        composeTestRule.waitForSnackbar(ScreenObjects.SETTINGS_UPDATED)
        composeTestRule.assertTextExists(ScreenObjects.SETTINGS_UPDATED)
    }

    @Test
    fun testSetPreferences_InternetConnectionError() {
        composeTestRule.clickButton(ScreenObjects.PREFERENCES_BUTTON)
        composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE)

        composeTestRule.clickButton(ScreenObjects.SUSHI)
        composeTestRule.clickButton(ScreenObjects.KOREAN)

        device.enableAirplaneMode()

        composeTestRule.clickButton(ScreenObjects.SAVE_PREFERENCES)
        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }

    @Test
    fun testSetPreferences_SelectAndDeselectPreferences() {
        composeTestRule.clickButton(ScreenObjects.PREFERENCES_BUTTON)
        composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE)

        composeTestRule.clickButton(ScreenObjects.SUSHI)
        composeTestRule.clickButton(ScreenObjects.ITALIAN)
        composeTestRule.clickButton(ScreenObjects.JAPANESE)
        composeTestRule.clickButton(ScreenObjects.CHINESE)

        composeTestRule.clickButton(ScreenObjects.ITALIAN)
        composeTestRule.clickButton(ScreenObjects.CHINESE)

        composeTestRule.clickButton(ScreenObjects.SAVE_PREFERENCES)
        composeTestRule.waitForSnackbar(ScreenObjects.SETTINGS_UPDATED)
        composeTestRule.assertTextExists(ScreenObjects.SETTINGS_UPDATED)
    }

    @Test
    fun testSetPreferences_GoBackWithoutSaving() {
        composeTestRule.clickButton(ScreenObjects.PREFERENCES_BUTTON)
        composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE)

        composeTestRule.clickButton(ScreenObjects.PIZZA)
        composeTestRule.clickButton(ScreenObjects.GO_BACK)

        composeTestRule.waitForText(ScreenObjects.PROFILE_BUTTON)
        composeTestRule.assertTextExists(ScreenObjects.PREFERENCES_BUTTON)
    }
}