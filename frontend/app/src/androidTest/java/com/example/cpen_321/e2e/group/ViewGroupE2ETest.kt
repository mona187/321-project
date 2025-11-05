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
 * E2E Tests for View Group History and View Group Details Use Cases
 *
 * Use Case: View Group History
 * Main success scenario:
 * 1. User presses "View Active Group" button on "HomeScreen"
 * 2. User is taken to "ViewGroupsScreen"
 * 3. Screen displays message based on if user is in group or not:
 *    - If user is in a group, screen displays group details
 *    - If user is not in a group, screen displays message "No active groups"
 *
 * Use Case: View Group Details
 * Preconditions: Group has completed voting and restaurant is selected
 * Main success scenario:
 * 1. User is on "ViewGroupsScreen"
 * 2. User clicks on "View Details" Button
 * 3. User is taken to "GroupScreen", user can see selected restaurant (name, location),
 *    members in group (how many members, name, credibility score, phone number)
 *
 * Failure scenarios:
 * 3.a Internet connection error
 *     3.a.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ViewGroupE2ETest {

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
        composeTestRule.waitForText(ScreenObjects.WELCOME_PREFIX, substring = true, timeoutMillis = 5000)
    }

    @Test
    fun testViewGroupHistory_WithActiveGroup() {
        composeTestRule.assertTextExists(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)

        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE, timeoutMillis = 5000)

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true) or
                    hasText("Restaurant", substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNode(
            hasText(ScreenObjects.MEMBER_PREFIX, substring = true) or
                    hasText("Member", substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNodeWithText(ScreenObjects.LEAVE_GROUP, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testViewGroupHistory_NoActiveGroup() {
        val viewGroupButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.VIEW_ACTIVE_GROUP) or
                    hasText(ScreenObjects.CURRENT_GROUPS),
            useUnmergedTree = true
        )

        if (viewGroupButton.fetchSemanticsNodes().isNotEmpty()) {
            viewGroupButton.onFirst().performClick()
        }

        composeTestRule.waitForText(
            ScreenObjects.NO_ACTIVE_GROUPS,
            timeoutMillis = 5000
        )

        composeTestRule.assertTextExists(ScreenObjects.NO_ACTIVE_GROUPS)

        composeTestRule.onNodeWithText(
            ScreenObjects.GO_TO_HOME,
            substring = true,
            useUnmergedTree = true
        ).assertExists()

        val noRestaurantInfo = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isEmpty()

        assert(noRestaurantInfo) { "No restaurant info should be shown" }
    }

    @Test
    fun testViewGroupHistory_ConnectionError() {
        device.enableAirplaneMode()

        val viewGroupButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.VIEW_ACTIVE_GROUP) or
                    hasText(ScreenObjects.CURRENT_GROUPS),
            useUnmergedTree = true
        )

        if (viewGroupButton.fetchSemanticsNodes().isNotEmpty()) {
            viewGroupButton.onFirst().performClick()
        }

        composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }

    @Test
    fun testViewGroupDetails_MainSuccessScenario() {
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE, timeoutMillis = 5000)

        val viewDetailsButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.VIEW_DETAILS),
            useUnmergedTree = true
        )

        if (viewDetailsButton.fetchSemanticsNodes().isNotEmpty()) {
            viewDetailsButton.onFirst().performClick()

            composeTestRule.waitForText(ScreenObjects.GROUP_TITLE, timeoutMillis = 3000)
        }

        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_NAME_PREFIX, substring = true) or
                    hasText("Restaurant", substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNode(
            hasText(ScreenObjects.LOCATION_PREFIX, substring = true) or
                    hasText("Location", substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onNode(
            hasText(ScreenObjects.MEMBER_PREFIX, substring = true) or
                    hasText("Member", substring = true),
            useUnmergedTree = true
        ).assertExists()

        val memberDetailsPresent = composeTestRule.onAllNodes(
            hasText("Name:", substring = true) or
                    hasText("Credibility Score:", substring = true) or
                    hasText("Phone Number:", substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()

        assert(memberDetailsPresent) { "Member details should be displayed" }

        composeTestRule.onNodeWithText(
            ScreenObjects.CLOSE_GROUP,
            substring = true,
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testViewGroupDetails_ConnectionError() {
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE, timeoutMillis = 5000)

        device.enableAirplaneMode()

        val viewDetailsButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.VIEW_DETAILS),
            useUnmergedTree = true
        )

        if (viewDetailsButton.fetchSemanticsNodes().isNotEmpty()) {
            viewDetailsButton.onFirst().performClick()

            composeTestRule.waitForText(ScreenObjects.CONNECTION_ERROR, timeoutMillis = 15000)
            composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)
        }

        device.disableAirplaneMode()
    }

    @Test
    fun testViewGroupDetails_VerifyAllMemberInfo() {
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE)

        val memberNodes = composeTestRule.onAllNodes(
            hasText(ScreenObjects.MEMBER_PREFIX, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes()

        if (memberNodes.isNotEmpty()) {
            assert(memberNodes.size >= 1) { "At least one member should be displayed" }

            composeTestRule.onNode(
                hasText("Name:", substring = true),
                useUnmergedTree = true
            ).assertExists()

            composeTestRule.onNode(
                hasText("Credibility Score:", substring = true),
                useUnmergedTree = true
            ).assertExists()

            composeTestRule.onNode(
                hasText("Phone Number:", substring = true),
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun testViewGroupDetails_ScrollThroughMembers() {
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE)

        composeTestRule.onRoot(useUnmergedTree = true)
            .performTouchInput {
                swipeUp()
            }

        Thread.sleep(500)

        composeTestRule.assertTextExists(ScreenObjects.GROUP_TITLE)

        composeTestRule.onRoot(useUnmergedTree = true)
            .performTouchInput {
                swipeDown()
            }
    }

    @Test
    fun testViewGroupDetails_NavigateBackToHome() {
        composeTestRule.clickButton(ScreenObjects.VIEW_ACTIVE_GROUP)
        composeTestRule.waitForText(ScreenObjects.GROUP_TITLE)

        device.pressBack()

        composeTestRule.waitForText(ScreenObjects.START_MATCHMAKING, timeoutMillis = 5000)
        composeTestRule.assertTextExists(ScreenObjects.START_MATCHMAKING)
    }
}