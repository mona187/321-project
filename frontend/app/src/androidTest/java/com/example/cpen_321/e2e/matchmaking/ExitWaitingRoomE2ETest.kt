package com.example.cpen_321.e2e.matchmaking

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
import com.example.cpen_321.e2e.utils.TestHelpers.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for Exit Waiting Room Use Case
 *
 * Use Case: Exit Waiting Room
 *
 * Main success scenario:
 * 1. User is in "WaitingRoomScreen" and clicks on "Leave Room" button
 * 2. Screen prompts user with a confirmation message, with options to stay or leave
 * 3. User clicks on leave, user is taken out of waiting room
 *
 * Failure scenarios:
 * 3.a User clicks on stay
 *     3.a.1 User stays in waiting room and does not exit the room
 * 3.b Internet connection error
 *     3.b.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExitWaitingRoomE2ETest {

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

        navigateToWaitingRoom()
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

    private fun navigateToWaitingRoom() {
        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING, timeoutMillis = 5000)
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE, timeoutMillis = 10000)
    }

    @Test
    fun testExitWaitingRoom_MainSuccessScenario() {
        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_ROOM)
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)

        composeTestRule.waitForText(ScreenObjects.LEAVE_CONFIRM_TITLE, timeoutMillis = 3000)

        composeTestRule.assertTextExists(ScreenObjects.STAY_BUTTON)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING, timeoutMillis = 5000)
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
        composeTestRule.assertTextExists(ScreenObjects.WELCOME_PREFIX, substring = true)
    }

    @Test
    fun testExitWaitingRoom_UserClicksStay() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)

        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON, timeoutMillis = 3000)

        composeTestRule.clickButton(ScreenObjects.STAY_BUTTON)

        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_ROOM)

        composeTestRule.onNode(
            hasText(ScreenObjects.TIME_REMAINING, substring = true) or
                    hasText(":", substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNode(
            hasContentDescription(ScreenObjects.DEFAULT_AVATAR_DESCRIPTION) or
                    hasContentDescription(ScreenObjects.PROFILE_PICTURE_DESCRIPTION),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testExitWaitingRoom_ConnectionError() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)

        device.enableAirplaneMode()

        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }

    @Test
    fun testExitWaitingRoom_CancelThenLeave() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)
        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON)
        composeTestRule.clickButton(ScreenObjects.STAY_BUTTON)

        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)

        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
    }

    @Test
    fun testExitWaitingRoom_DialogDismissalWithBackButton() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)
        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON)

        device.pressBack()

        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_ROOM)
    }

    @Test
    fun testExitWaitingRoom_VerifyCleanupAfterLeave() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)

        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE, timeoutMillis = 10000)

        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_ROOM)
    }
}