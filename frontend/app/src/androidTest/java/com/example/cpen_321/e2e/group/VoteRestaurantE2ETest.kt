package com.example.cpen_321.e2e.group

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.cpen_321.MainActivity
import com.example.cpen_321.e2e.utils.ScreenObjects
import com.example.cpen_321.e2e.utils.TestHelpers.assertTextExists
import com.example.cpen_321.e2e.utils.TestHelpers.clickButton
import com.example.cpen_321.e2e.utils.TestHelpers.disableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.enableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.grantLocationPermission
import com.example.cpen_321.e2e.utils.TestHelpers.revokeLocationPermission
import com.example.cpen_321.e2e.utils.TestHelpers.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for Vote on Restaurant Use Case
 *
 * Use Case: Vote on Restaurant
 *
 * Main success scenario:
 * 1. User is taken to "VoteRestaurantScreen"
 * 2. User selects a choice from list of restaurants presented
 * 3. User confirms their vote by selecting "Submit Vote" button
 * 4. Number of votes is updated to include their vote
 * 5. User waits for other users in group to vote
 *
 * Failure scenarios:
 * 2.a List of restaurants is not provided because user did not provide location permissions
 *     2.a.1 User will not receive list of restaurants
 *     2.a.2 User must go into phone settings and enable location for this app
 *     2.a.3 User then goes back into app and list of restaurants is provided
 * 2.b Internet connection error, restaurants not able to be retrieved
 *     2.b.1 Screen is waiting for response, user is prompted to try again at a later time
 * 3.a Internet connection error
 *     3.a.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VoteRestaurantE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private val packageName = "com.example.cpen_321"

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

        navigateToVotingScreen()
    }

    @After
    fun tearDown() {
        device.disableAirplaneMode()
        device.grantLocationPermission(packageName)

        // Navigate home
        try {
            composeTestRule.onNodeWithContentDescription("Home").performClick()
            composeTestRule.waitForIdle()
        } catch (e: Exception) {
            // Already at home
        }
    }

    private fun navigateToVotingScreen() {
        try {
            composeTestRule.waitForText(
                ScreenObjects.VOTE_RESTAURANT_TITLE,
                timeoutMillis = 5000
            )
        } catch (e: Exception) {
            android.util.Log.d("E2E_Test", "Navigate to voting screen via matchmaking")
        }
    }

    @Test
    fun testVoteRestaurant_MainSuccessScenario() {
        composeTestRule.assertTextExists(ScreenObjects.VOTE_RESTAURANT_TITLE, substring = true)
        composeTestRule.assertTextExists(ScreenObjects.SELECT_RESTAURANT, substring = true)

        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyDescendant(
                hasText(ScreenObjects.RESTAURANT_OPTION, substring = true)
            ),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.onNodeWithContentDescription("Thumbs Up", useUnmergedTree = true)
            .assertExists()
            .performClick()

        composeTestRule.waitForText(ScreenObjects.VOTE_RECORDED, timeoutMillis = 5000)
        composeTestRule.assertTextExists(ScreenObjects.VOTE_RECORDED)

        composeTestRule.onNode(
            hasContentDescription("Vote Count") or
                    hasText("vote", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()

        composeTestRule.assertTextExists(ScreenObjects.VOTE_RESTAURANT_TITLE, substring = true)
    }

    @Test
    fun testVoteRestaurant_NoLocationPermission() {
        device.revokeLocationPermission(packageName)
        Thread.sleep(1000)

        composeTestRule.activityRule.scenario.recreate()

        composeTestRule.waitForText(
            ScreenObjects.LOCATION_PERMISSION,
            timeoutMillis = 10000
        )
        composeTestRule.assertTextExists(ScreenObjects.LOCATION_PERMISSION)

        composeTestRule.onNodeWithText(
            ScreenObjects.GRANT_PERMISSION,
            useUnmergedTree = true
        ).assertExists()

        device.grantLocationPermission(packageName)

        composeTestRule.activityRule.scenario.recreate()

        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )
        composeTestRule.assertTextExists(ScreenObjects.RESTAURANT_OPTION, substring = true)
    }

    @Test
    fun testVoteRestaurant_ConnectionErrorLoadingRestaurants() {
        device.enableAirplaneMode()

        composeTestRule.activityRule.scenario.recreate()

        composeTestRule.waitForText(
            ScreenObjects.CONNECTION_ERROR,
            timeoutMillis = 15000
        )
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        val restaurantsVisible = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isEmpty()

        assert(restaurantsVisible) { "No restaurants should be visible without connection" }

        device.disableAirplaneMode()
    }

    @Test
    fun testVoteRestaurant_ConnectionErrorSubmittingVote() {
        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )

        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyDescendant(
                hasText(ScreenObjects.RESTAURANT_OPTION, substring = true)
            ),
            useUnmergedTree = true
        ).onFirst().performClick()

        device.enableAirplaneMode()

        composeTestRule.onNodeWithContentDescription("Thumbs Up", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitForText(
            ScreenObjects.CONNECTION_ERROR,
            timeoutMillis = 15000
        )
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        device.disableAirplaneMode()
    }

    @Test
    fun testVoteRestaurant_ThumbsDown() {
        composeTestRule.waitForText(ScreenObjects.RESTAURANT_OPTION, substring = true)

        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyDescendant(
                hasText(ScreenObjects.RESTAURANT_OPTION, substring = true)
            ),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.onNodeWithContentDescription("Thumbs Down", useUnmergedTree = true)
            .assertExists()
            .performClick()

        composeTestRule.waitForText(ScreenObjects.VOTE_RECORDED, timeoutMillis = 5000)
        composeTestRule.assertTextExists(ScreenObjects.VOTE_RECORDED)
    }

    @Test
    fun testVoteRestaurant_ChangeVote() {
        composeTestRule.waitForText(ScreenObjects.RESTAURANT_OPTION, substring = true)

        composeTestRule.onAllNodes(
            hasClickAction() and hasAnyDescendant(
                hasText(ScreenObjects.RESTAURANT_OPTION, substring = true)
            ),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.onNodeWithContentDescription("Thumbs Up", useUnmergedTree = true)
            .performClick()

        composeTestRule.waitForText(ScreenObjects.VOTE_RECORDED, timeoutMillis = 5000)

        val changeVoteButton = composeTestRule.onAllNodes(
            hasText(ScreenObjects.CHANGE_VOTE, substring = true),
            useUnmergedTree = true
        )

        if (changeVoteButton.fetchSemanticsNodes().isNotEmpty()) {
            changeVoteButton.onFirst().performClick()

            composeTestRule.onNodeWithContentDescription("Thumbs Down", useUnmergedTree = true)
                .performClick()

            composeTestRule.waitForText(ScreenObjects.VOTE_RECORDED)
        }
    }

    @Test
    fun testVoteRestaurant_SwipeBetweenRestaurants() {
        composeTestRule.waitForText(ScreenObjects.RESTAURANT_OPTION, substring = true)

        composeTestRule.onRoot(useUnmergedTree = true)
            .performTouchInput {
                swipeLeft()
            }

        Thread.sleep(500)

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).assertExists()
    }
}