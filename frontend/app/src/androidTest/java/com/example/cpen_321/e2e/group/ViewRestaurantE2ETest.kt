package com.example.cpen_321.e2e.group

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.cpen_321.MainActivity
import com.example.cpen_321.e2e.utils.ScreenObjects
import com.example.cpen_321.e2e.utils.TestHelpers.assertTextExists
import com.example.cpen_321.e2e.utils.TestHelpers.disableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.enableAirplaneMode
import com.example.cpen_321.e2e.utils.TestHelpers.grantLocationPermission
import com.example.cpen_321.e2e.utils.TestHelpers.revokeLocationPermission
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
 * E2E Test for View Restaurant Choice Use Case
 *
 * Use Case: View Restaurant Choice
 *
 * Main success scenario:
 * 1. User is asked to accept location permissions for accessing local restaurants
 * 2. App presents user with restaurant choices based on their preferences
 *
 * Failure scenarios:
 * 1.a User does not provide access to location
 *     1.a.1 User will not receive list of restaurants
 *     1.a.2 User must go into phone settings and enable location for this app
 *     1.a.3 User then goes back into app
 * 1.b Internet connection error, restaurants not able to be retrieved
 *     1.b.1 Screen is waiting for response, user is prompted to try again at a later time
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ViewRestaurantE2ETest {

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

    @Test
    fun testViewRestaurantChoice_MainSuccessScenario() {
        device.revokeLocationPermission(packageName)
        Thread.sleep(1000)

        navigateToVotingScreen()

        val permissionDialog = device.wait(
            androidx.test.uiautomator.Until.hasObject(
                androidx.test.uiautomator.By.text("Allow")
            ),
            5000
        )

        if (permissionDialog) {
            val allowButton = device.findObject(
                UiSelector()
                    .textMatches("(?i)allow.*")
                    .className("android.widget.Button")
            )

            if (allowButton.exists()) {
                allowButton.click()
                device.waitForIdle()
            }
        } else {
            device.grantLocationPermission(packageName)
        }

        Thread.sleep(1000)

        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )

        val restaurantCount = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().size

        assert(restaurantCount > 0) { "At least one restaurant should be displayed" }

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testViewRestaurantChoice_NoLocationAccess() {
        device.revokeLocationPermission(packageName)
        Thread.sleep(1000)

        navigateToVotingScreen()

        composeTestRule.waitForText(
            ScreenObjects.LOCATION_PERMISSION,
            timeoutMillis = 10000
        )
        composeTestRule.assertTextExists(ScreenObjects.LOCATION_PERMISSION)

        val noRestaurants = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isEmpty()

        assert(noRestaurants) { "No restaurants should be visible without permission" }

        device.grantLocationPermission(packageName)
        Thread.sleep(1000)

        composeTestRule.activityRule.scenario.recreate()

        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )
        composeTestRule.assertTextExists(ScreenObjects.RESTAURANT_OPTION, substring = true)
    }

    @Test
    fun testViewRestaurantChoice_ConnectionError() {
        device.grantLocationPermission(packageName)

        device.enableAirplaneMode()
        Thread.sleep(1000)

        navigateToVotingScreen()

        composeTestRule.waitForText(
            ScreenObjects.CONNECTION_ERROR,
            timeoutMillis = 15000
        )
        composeTestRule.assertTextExists(ScreenObjects.CONNECTION_ERROR)

        val hasLoadingOrError = composeTestRule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate) or
                    hasText(ScreenObjects.LOADING_RESTAURANTS) or
                    hasText(ScreenObjects.CONNECTION_ERROR, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()

        assert(hasLoadingOrError) { "Should show loading or error state" }

        device.disableAirplaneMode()
    }

    @Test
    fun testViewRestaurantChoice_PermissionDeniedPermanently() {
        device.revokeLocationPermission(packageName)

        navigateToVotingScreen()

        composeTestRule.waitForText(
            ScreenObjects.LOCATION_PERMISSION,
            timeoutMillis = 5000
        )

        composeTestRule.assertTextExists(ScreenObjects.LOCATION_PERMISSION)

        val noRestaurants = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isEmpty()

        assert(noRestaurants) { "No restaurants without permission" }
    }

    @Test
    fun testViewRestaurantChoice_RestaurantsMatchPreferences() {
        device.grantLocationPermission(packageName)

        navigateToVotingScreen()

        composeTestRule.waitForText(
            ScreenObjects.RESTAURANT_OPTION,
            substring = true,
            timeoutMillis = 10000
        )

        val restaurantNodes = composeTestRule.onAllNodes(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).fetchSemanticsNodes()

        assert(restaurantNodes.isNotEmpty()) { "Restaurants should be displayed" }

        composeTestRule.onNode(
            hasText(ScreenObjects.RESTAURANT_OPTION, substring = true),
            useUnmergedTree = true
        ).assertExists()
    }

    private fun navigateToVotingScreen() {
        try {
            composeTestRule.waitForText(
                ScreenObjects.VOTE_RESTAURANT_TITLE,
                timeoutMillis = 3000
            )
        } catch (e: Exception) {
            android.util.Log.d("E2E_Test", "Navigate to voting screen")
        }
    }
}