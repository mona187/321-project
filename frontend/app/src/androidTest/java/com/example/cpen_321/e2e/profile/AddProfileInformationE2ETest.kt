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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for Add Profile Information Use Case
 *
 * Main success scenario:
 * 1. User is on "ProfileConfigScreen" and clicks on "Profile" button
 * 2. User is brought to "ProfileScreen"
 * 3. User adds profile picture by clicking on "Change Profile Picture" button
 * 4. User adds name by clicking on "Name" textfield and typing the updated name
 * 5. User adds bio by clicking on "Bio" textfield and typing the updated bio
 * 6. User adds phone number by clicking on "Phone Number" textfield and typing the updated phone number
 * 7. User presses "Save Profile" button to save profile
 * 8. The screen refreshes and "Settings updated successfully" message is printed
 *
 * Failure scenarios:
 * 6. Phone number entered is less than 10 digits
 *    6.a.1 The screen prints an error message "Phone number must be at least 10 digits"
 *    6.a.2 User is prompted to enter phone number again
 * 8. Internet connection error
 *    8.a.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddProfileInformationE2ETest {

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

        navigateToProfileScreen()
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

    private fun navigateToProfileScreen() {
        composeTestRule.waitForText("Welcome", substring = true, timeoutMillis = 5000)

        composeTestRule.onAllNodesWithContentDescription("Profile", useUnmergedTree = true)
            .onLast()
            .performClick()

        composeTestRule.waitForText(ScreenObjects.PROFILE_BUTTON, timeoutMillis = 3000)
        composeTestRule.clickButton(ScreenObjects.PROFILE_BUTTON)
        composeTestRule.waitForText(ScreenObjects.CHANGE_PROFILE_PICTURE, timeoutMillis = 3000)
    }

    @Test
    fun testAddProfileInformation_MainSuccessScenario() {
        composeTestRule.onNodeWithText(ScreenObjects.CHANGE_PROFILE_PICTURE)
            .assertExists()
            .performClick()

        Thread.sleep(500)
        device.pressBack()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.NAME_LABEL)),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.NAME_LABEL)),
            useUnmergedTree = true
        ).performTextInput("John Doe")

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.BIO_LABEL)),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.BIO_LABEL)),
            useUnmergedTree = true
        ).performTextInput("Food enthusiast and social eater")

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextInput("6041234567")

        composeTestRule.onNodeWithText(ScreenObjects.SAVE_PROFILE)
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForSnackbar(ScreenObjects.SETTINGS_UPDATED, timeoutMillis = 10000)
        composeTestRule.assertTextExists(ScreenObjects.SETTINGS_UPDATED)
    }

    @Test
    fun testAddProfileInformation_InvalidPhoneNumber() {
        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.NAME_LABEL)),
            useUnmergedTree = true
        ).performTextInput("Test User")

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextInput("123")

        composeTestRule.waitForText(ScreenObjects.PHONE_ERROR_MIN, timeoutMillis = 3000)
        composeTestRule.assertTextExists(ScreenObjects.PHONE_ERROR_MIN)

        composeTestRule.onNodeWithText(ScreenObjects.SAVE_PROFILE)
            .assertIsNotEnabled()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextInput("6041234567")

        Thread.sleep(500)

        composeTestRule.onNodeWithText(ScreenObjects.SAVE_PROFILE)
            .assertIsEnabled()
    }

    @Test
    fun testAddProfileInformation_ConnectionError() {
        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.NAME_LABEL)),
            useUnmergedTree = true
        ).performTextInput("Test User")

        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText(ScreenObjects.PHONE_NUMBER_LABEL)),
            useUnmergedTree = true
        ).performTextInput("6041234567")

        device.enableAirplaneMode()

        composeTestRule.onNodeWithText(ScreenObjects.SAVE_PROFILE)
            .performClick()

        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }
}