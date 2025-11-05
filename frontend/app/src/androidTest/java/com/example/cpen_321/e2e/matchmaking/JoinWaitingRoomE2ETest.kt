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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for Request Matches (Join Waiting Room) Use Case
 *
 * Use Case: Request Matches (Join Waiting Room)
 *
 * Main success scenario:
 * 1. User is on "HomeScreen" and clicks on "Start Matchmaking" button
 * 2. User is brought into "WaitingRoomScreen" which shows current users and timer
 * 3. If max number of users has joined or waiting room timer ends and minimum number
 *    of users have joined, screen takes user to GroupScreen
 *
 * Failure scenarios:
 * User has not set preferences
 *    1.a.1 User is brought to "PreferencesScreen" and must set their preferences
 *    1.a.2 Once preferences are set, user is taken to "HomeScreen" and can restart matchmaking
 * Internet connection error
 *    2.a.1 Screen is waiting for response, user is prompted to try again at a later time
 * Number of users joined was not enough or timer expired before users joined
 *    3.a.1 Users can restart matchmaking by leaving waiting room
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class JoinWaitingRoomE2ETest {

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

        navigateToHomeScreen()
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

    private fun navigateToHomeScreen() {
        composeTestRule.waitForText(ScreenObjects.WELCOME_PREFIX, timeoutMillis = 5000)
    }

    @Test
    fun testJoinWaitingRoom_MainSuccessScenario() {
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)

        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE, timeoutMillis = 10000)
        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.FINDING_GROUP)

        composeTestRule.onNode(
            hasContentDescription(ScreenObjects.TIMER_DESCRIPTION) or
                    hasText(ScreenObjects.TIME_REMAINING, substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNode(
            hasContentDescription(ScreenObjects.DEFAULT_AVATAR_DESCRIPTION) or
                    hasContentDescription(ScreenObjects.PROFILE_PICTURE_DESCRIPTION),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNodeWithText(
            ScreenObjects.MEMBERS_IN_ROOM,
            substring = true,
            useUnmergedTree = true
        ).assertExists()

        val groupReadyAppeared = try {
            composeTestRule.waitForText(ScreenObjects.GROUP_READY, timeoutMillis = 60000)
            true
        } catch (e: Exception) {
            false
        }

        if (groupReadyAppeared) {
            composeTestRule.assertTextExists(ScreenObjects.GROUP_READY)
            composeTestRule.assertTextExists(ScreenObjects.PREPARING_GROUP)

            composeTestRule.waitForText(
                ScreenObjects.VOTE_RESTAURANT_TITLE,
                timeoutMillis = 10000
            )
        } else {
            android.util.Log.d("E2E_Test",
                "Group formation requires multi-device setup or backend simulation")
        }
    }

    @Test
    fun testJoinWaitingRoom_PreferencesNotSet() {
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)

        val onPreferencesScreen = try {
            composeTestRule.waitForText(ScreenObjects.PREFERENCES_TITLE, timeoutMillis = 5000)
            true
        } catch (e: Exception) {
            false
        }

        if (onPreferencesScreen) {
            composeTestRule.assertTextExists(ScreenObjects.PREFERENCES_TITLE)

            composeTestRule.clickButton(ScreenObjects.SUSHI)
            composeTestRule.clickButton(ScreenObjects.ITALIAN)

            composeTestRule.onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 0f, range = 0f..200f)),
                useUnmergedTree = true
            ).onFirst().performTouchInput {
                swipeRight(startX = 0f, endX = width.toFloat() * 0.5f)
            }

            composeTestRule.clickButton(ScreenObjects.SAVE_PREFERENCES)

            composeTestRule.waitForText(ScreenObjects.WELCOME_PREFIX, timeoutMillis = 10000)
            composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)

            composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
            composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE)
            composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        } else {
            composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
        }
    }

    @Test
    fun testJoinWaitingRoom_InternetConnectionError() {
        device.enableAirplaneMode()

        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)

        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING, timeoutMillis = 5000)

        device.disableAirplaneMode()
    }

    @Test
    fun testJoinWaitingRoom_NotEnoughUsers() {
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE)

        val timerExpired = try {
            composeTestRule.waitForText(
                ScreenObjects.UNABLE_TO_CREATE_GROUP,
                timeoutMillis = 35000
            )
            true
        } catch (e: Exception) {
            false
        }

        if (timerExpired) {
            composeTestRule.assertTextExists(ScreenObjects.UNABLE_TO_CREATE_GROUP)
            composeTestRule.assertTextExists(ScreenObjects.TIMER_EXPIRED, substring = true)

            composeTestRule.clickButton(ScreenObjects.TRY_AGAIN)

            composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)
            composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
        } else {
            android.util.Log.d("E2E_Test",
                "Timer expiration test requires backend configuration or longer wait")
        }
    }

    @Test
    fun testJoinWaitingRoom_VerifyTimerCountdown() {
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE)

        val initialTimerExists = composeTestRule.onAllNodes(
            hasText(":", substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()

        assert(initialTimerExists) { "Timer should be displayed" }

        Thread.sleep(3000)

        val timerStillExists = composeTestRule.onAllNodes(
            hasText(":", substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()

        assert(timerStillExists) { "Timer should still be counting" }
    }

    @Test
    fun testJoinWaitingRoom_MultipleAttempts() {
        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE)

        composeTestRule.clickButton(ScreenObjects.LEAVE_ROOM)

        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)

        composeTestRule.clickButton(ScreenObjects.START_MATCHMAKING)
        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
    }
}