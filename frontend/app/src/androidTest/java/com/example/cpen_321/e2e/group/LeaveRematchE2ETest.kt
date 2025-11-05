package com.example.cpen_321.e2e.group

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
 * E2E Test for Leave/Rematch Use Case
 *
 * Use Case: Leave/Rematch
 *
 * Main success scenario:
 * 1. User is on "ViewGroupsScreen"
 * 2. User presses "Leave Group" button
 * 3. Confirmation message is given, two choices "stay" or "leave"
 * 4. User presses "leave", user has left the group
 * 5. User can rematch by pressing "Start Matchmaking" button in "HomeScreen"
 *
 * Failure scenarios:
 * 4.a User clicks on stay
 *     4.a.1 User stays in waiting room and does not exit the room
 * 4.b Internet connection error
 *     4.b.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LeaveRematchE2ETest {

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

        navigateToViewGroupsScreen()
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

    private fun navigateToViewGroupsScreen() {
        try {
            composeTestRule.waitForText(ScreenObjects.VIEW_ACTIVE_GROUP, timeoutMillis = 5000)
            composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
            composeTestRule.waitForText(ScreenObjects.GROUP_TITLE, timeoutMillis = 5000)
        } catch (e: Exception) {
            android.util.Log.d("E2E_Test", "Navigate to ViewGroupsScreen")
        }
    }

    @Test
    fun testLeaveRematch_MainSuccessScenario() {
        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true) or
                    hasText(ScreenObjects.MEMBER_PREFIX, substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.assertTextExists(ScreenObjects.LEAVE_GROUP)
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)

        composeTestRule.waitForText(ScreenObjects.LEAVE_GROUP_CONFIRM, substring = true)
        composeTestRule.assertTextExists(ScreenObjects.STAY_BUTTON)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING, timeoutMillis = 5000)
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
        composeTestRule.assertTextExists(ScreenObjects.WELCOME_PREFIX, substring = true)

        composeTestRule.onNodeWithText(ScreenObjects.START_MATCHMAKING)
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForText(ScreenObjects.WAITING_ROOM_TITLE, timeoutMillis = 10000)
        composeTestRule.assertTextExists(ScreenObjects.WAITING_ROOM_TITLE)
    }

    @Test
    fun testLeaveRematch_UserClicksStay() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)

        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON)

        composeTestRule.clickButton(ScreenObjects.STAY_BUTTON)

        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_GROUP)

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true) or
                    hasText(ScreenObjects.MEMBER_PREFIX, substring = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testLeaveRematch_ConnectionError() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)

        device.enableAirplaneMode()

        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }

    @Test
    fun testLeaveRematch_CancelThenLeave() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON)
        composeTestRule.clickButton(ScreenObjects.STAY_BUTTON)

        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)

        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
    }

    @Test
    fun testLeaveRematch_MultipleRematchCycles() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)

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

    @Test
    fun testLeaveRematch_BackButtonDismissesDialog() {
        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.STAY_BUTTON)

        device.pressBack()

        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)
        composeTestRule.assertTextExists(ScreenObjects.LEAVE_GROUP)
    }

    @Test
    fun testLeaveRematch_VerifyGroupDataCleared() {
        val hadRestaurantInfo = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()

        composeTestRule.clickButton(ScreenObjects.LEAVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.clickButton(ScreenObjects.LEAVE_BUTTON)
        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING)

        composeTestRule.waitForText(ScreenObjects.CURRENT_GROUPS, timeoutMillis = 3000)

        val currentGroupsButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.CURRENT_GROUPS) or
                    hasText(ScreenObjects.VIEW_ACTIVE_GROUP),
            useUnmergedTree = true
        )

        if (currentGroupsButton.fetchSemanticsNodes().isNotEmpty()) {
            currentGroupsButton.onFirst().performClick()

            composeTestRule.waitForText(ScreenObjects.NO_ACTIVE_GROUPS, timeoutMillis = 5000)
            composeTestRule.assertTextExists(ScreenObjects.NO_ACTIVE_GROUPS)
        }
    }
}