package com.example.cpen_321.test

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import android.content.Context
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.junit.After
import com.example.cpen_321.MainActivity // Update with your actual MainActivity

/**
 * End-to-End Test Suite for FeastFriends App
 *
 * Test Location: app/src/androidTest/java/com/example/cpen_321/test/
 *
 * IMPORTANT PREREQUISITES FOR TAs:
 * 1. Log into the app with the provided testing account BEFORE running tests
 * 2. The app should be on the home/main screen when tests begin
 * 3. Ensure location permissions are granted to the app
 * 4. Ensure stable internet connection
 *
 * Test Coverage:
 * - Feature 1: Profile Creation (Set Preferences, Add/Update Profile Information)
 * - Feature 2: Matchmaking (Join/Exit Waiting Room)
 * - Feature 3: Group Creation and Restaurant Voting
 *
 * Each test is STANDALONE and does NOT depend on other tests.
 * Tests clean up their state at the end to ensure independence.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FeastFriendsE2ETests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Verify we're on the home screen (user should already be logged in)
        waitForHomeScreen()
    }

    @After
    fun tearDown() {
        // Return to home screen after each test
        navigateToHome()
    }

    /**
     * Waits for the home screen to appear
     * Assumes user is already logged in as per requirement #4
     */
    private fun waitForHomeScreen() {
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Start Matchmaking")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Navigate back to home screen for test independence
     */
    private fun navigateToHome() {
        try {
            // Try clicking Home navigation button
            composeTestRule.onNodeWithContentDescription("Home")
                .performClick()
            Thread.sleep(1000)
        } catch (e: Exception) {
            // Already on home or navigation failed
        }
    }

    // ==================== FEATURE 1: PROFILE CREATION ====================

    /**
     * Test Case 1.1: Set Preferences - Main Success Scenario
     *
     * STANDALONE TEST - No prerequisites needed
     *
     * Use Case: Set Preferences
     * Expected Behavior: User successfully sets and saves preferences
     *
     * Steps:
     * 1. Navigate to Profile section
     * 2. Open Preferences screen
     * 3. Select cuisine preferences (Sushi, Italian)
     * 4. Adjust budget slider
     * 5. Adjust search radius slider
     * 6. Save preferences
     * 7. Verify success message
     */
    @Test
    fun test_01_SetPreferences_Success() {
        // Verify starting from home screen
        composeTestRule.onNodeWithText("Start Matchmaking")
            .assertExists()

        // Step 1: Navigate to ProfileConfigScreen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Preferences")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Step 2: Click on Preferences button
        composeTestRule.onNodeWithText("Preferences")
            .performClick()

        // Step 3: Wait for PreferencesScreen to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Preferences (Select)")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Step 4: Select cuisine preferences
        composeTestRule.onNodeWithText("Sushi")
            .performClick()
        composeTestRule.onNodeWithText("Italian")
            .performClick()

        // Step 5: Set budget using slider
        composeTestRule.onNodeWithText("Max amount of money to spend: $", substring = true)
            .assertExists()

        composeTestRule.onAllNodesWithTag("BudgetSlider", useUnmergedTree = true)
            .onFirst()
            .performTouchInput {
                swipeRight(startX = centerX - 100f, endX = centerX + 50f)
            }

        // Step 6: Set search radius using slider
        composeTestRule.onNodeWithText("Search radius:", substring = true)
            .assertExists()

        composeTestRule.onAllNodesWithTag("RadiusSlider", useUnmergedTree = true)
            .onFirst()
            .performTouchInput {
                swipeRight(startX = centerX - 50f, endX = centerX + 50f)
            }

        // Step 7: Save preferences
        composeTestRule.onNodeWithText("Save Preferences")
            .performClick()

        // Step 8: Verify success message from REAL backend
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Settings updated successfully")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Cleanup: Return to home
        navigateToHome()
    }

    /**
     * Test Case 1.2: Set Preferences - Network Error
     *
     * STANDALONE TEST - No prerequisites needed
     *
     * Use Case: Set Preferences - Failure Scenario 7
     * Expected Behavior: App handles network error gracefully
     *
     * Note: This test temporarily disables network connectivity
     */
    @Test
    fun test_02_SetPreferences_NetworkError() {
        // Enable airplane mode to simulate network error
        device.executeShellCommand("cmd connectivity airplane-mode enable")
        Thread.sleep(2000) // Wait for airplane mode to activate

        try {
            // Navigate to Preferences
            composeTestRule.onNodeWithContentDescription("Profile")
                .performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Preferences")
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            composeTestRule.onNodeWithText("Preferences")
                .performClick()

            // Wait for screen to load
            Thread.sleep(2000)

            // Select a preference
            composeTestRule.onNodeWithText("Pizza")
                .performClick()

            // Try to save (should fail due to no network)
            composeTestRule.onNodeWithText("Save Preferences")
                .performClick()

            // Verify error message appears
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                try {
                    composeTestRule.onNode(
                        hasText("Error", substring = true) or
                                hasText("Failed", substring = true) or
                                hasText("network", substring = true, ignoreCase = true)
                    ).assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        } finally {
            // Cleanup: Re-enable network
            device.executeShellCommand("cmd connectivity airplane-mode disable")
            Thread.sleep(2000)
            navigateToHome()
        }
    }

    /**
     * Test Case 1.3: Add Profile Information - Success
     *
     * STANDALONE TEST - No prerequisites needed
     *
     * Use Case: Add Profile Information
     * Expected Behavior: User successfully adds/updates profile information
     *
     * Steps:
     * 1. Navigate to Profile screen
     * 2. Clear existing data
     * 3. Enter name, bio, and phone number
     * 4. Save profile
     * 5. Verify success message from backend
     */
    @Test
    fun test_03_AddProfileInfo_Success() {
        // Navigate to Profile screen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Profile")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Wait for ProfileScreen to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Name:")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Clear and enter name
        composeTestRule.onAllNodes(hasSetTextAction())
            .filterToOne(hasText("Name:", substring = true))
            .performTextClearance()
            .performTextInput("Test User E2E")

        // Clear and enter bio
        composeTestRule.onAllNodes(hasSetTextAction())
            .filterToOne(hasText("Bio:", substring = true))
            .performTextClearance()
            .performTextInput("Automated test user - Food enthusiast")

        // Clear and enter phone number
        composeTestRule.onAllNodes(hasSetTextAction())
            .filterToOne(hasText("Phone Number:", substring = true))
            .performTextClearance()
            .performTextInput("6041234567")

        // Save profile (interacts with REAL backend)
        composeTestRule.onNodeWithText("Save Profile")
            .performClick()

        // Verify success from backend
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Settings updated successfully")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Cleanup: Return to home
        navigateToHome()
    }

    /**
     * Test Case 1.4: Add Profile Information - Invalid Phone Number
     *
     * STANDALONE TEST - No prerequisites needed
     *
     * Use Case: Add Profile Information - Failure Scenario 6
     * Expected Behavior: App validates phone number and shows error
     */
    @Test
    fun test_04_AddProfileInfo_InvalidPhone() {
        // Navigate to Profile screen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Profile")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Wait for screen to load
        Thread.sleep(2000)

        // Clear and enter invalid phone number (less than 10 digits)
        composeTestRule.onAllNodes(hasSetTextAction())
            .filterToOne(hasText("Phone Number:", substring = true))
            .performTextClearance()
            .performTextInput("12345")

        // Try to save (should fail validation)
        composeTestRule.onNodeWithText("Save Profile")
            .performClick()

        // Verify validation error message
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Phone number must be at least 10 digits")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Verify save button is disabled
        composeTestRule.onNodeWithText("Save Profile")
            .assertIsNotEnabled()

        // Cleanup: Return to home
        navigateToHome()
    }

    // ==================== FEATURE 2: MATCHMAKING ====================

    /**
     * Test Case 2.1: Join Waiting Room - Success
     *
     * STANDALONE TEST - Automatically sets up prerequisites
     *
     * Use Case: Request Matches (Join Waiting Room)
     * Expected Behavior: User successfully joins waiting room
     *
     * Prerequisites (automated):
     * - User preferences must be set
     *
     * Steps:
     * 1. Ensure preferences are set (automated setup)
     * 2. Click Start Matchmaking
     * 3. Verify waiting room screen appears
     * 4. Verify timer and member count are displayed
     * 5. Leave room (cleanup)
     */
    @Test
    fun test_05_JoinWaitingRoom_Success() {
        // AUTOMATED SETUP: Ensure preferences are set
        setupPreferencesForMatchmaking()

        // Return to home screen
        navigateToHome()

        // Verify on home screen
        composeTestRule.onNodeWithText("Start Matchmaking")
            .assertExists()

        // Step 1: Click Start Matchmaking
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Step 2: Verify WaitingRoomScreen is displayed (interacts with REAL backend)
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Waiting Room")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Step 3: Verify timer is displayed
        composeTestRule.onNodeWithText("Time remaining", substring = true)
            .assertExists()

        // Step 4: Verify member count is displayed
        composeTestRule.onNodeWithText("Members", substring = true)
            .assertExists()

        // Wait a moment to observe the room
        Thread.sleep(3000)

        // CLEANUP: Leave room to ensure test independence
        leaveWaitingRoom()
    }

    /**
     * Test Case 2.2: Exit Waiting Room - Confirm Leave
     *
     * STANDALONE TEST - Automatically sets up prerequisites
     *
     * Use Case: Exit Waiting Room
     * Expected Behavior: User successfully leaves waiting room
     *
     * Steps:
     * 1. Join waiting room (automated)
     * 2. Click Leave Room button
     * 3. Verify confirmation dialog
     * 4. Confirm leave
     * 5. Verify returned to home screen
     */
    @Test
    fun test_06_ExitWaitingRoom() {
        // AUTOMATED SETUP
        setupPreferencesForMatchmaking()
        navigateToHome()

        // Join waiting room
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Wait for room to load
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Leave Room")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Step 1: Click Leave Room button
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()

        // Step 2: Verify confirmation dialog appears
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Leave Waiting Room?")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Are you sure you want to leave?", substring = true)
            .assertExists()

        // Step 3: Click Leave in dialog
        composeTestRule.onNodeWithText("Leave")
            .performClick()

        // Step 4: Verify back on HomeScreen (backend removes user from room)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Start Matchmaking")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Test Case 2.3: Exit Waiting Room - Cancel (Stay)
     *
     * STANDALONE TEST - Automatically sets up prerequisites
     *
     * Use Case: Exit Waiting Room - Failure Scenario 3a
     * Expected Behavior: User stays in waiting room when choosing Stay
     */
    @Test
    fun test_07_ExitWaitingRoom_Stay() {
        // AUTOMATED SETUP
        setupPreferencesForMatchmaking()
        navigateToHome()

        // Join waiting room
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Leave Room")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Click Leave Room
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()

        // Wait for dialog
        Thread.sleep(1000)

        // Click Stay in dialog
        composeTestRule.onNodeWithText("Stay")
            .performClick()

        // Verify still in waiting room
        composeTestRule.onNodeWithText("Waiting Room")
            .assertExists()
        composeTestRule.onNodeWithText("Time remaining", substring = true)
            .assertExists()

        // CLEANUP: Actually leave for test independence
        leaveWaitingRoom()
    }

    // ==================== FEATURE 3: GROUP CREATION & VOTING ====================

    /**
     * Test Case 3.1: View Group History
     *
     * STANDALONE TEST - No prerequisites needed
     *
     * Use Case: View Group History
     * Expected Behavior: User can view their group history (active or empty)
     *
     * Note: This test works regardless of whether user has an active group
     */
    @Test
    fun test_08_ViewGroupHistory() {
        // Verify starting from home
        composeTestRule.onNodeWithText("Start Matchmaking")
            .assertExists()

        // Step 1: Click View Active Group/Current Groups button
        val buttonText = try {
            composeTestRule.onNodeWithText("View Active Group")
                .assertExists()
            "View Active Group"
        } catch (e: AssertionError) {
            "Current Groups"
        }

        composeTestRule.onNodeWithText(buttonText)
            .performClick()

        // Step 2: Verify ViewGroupsScreen is displayed
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                // Either shows active group or no group message
                composeTestRule.onNode(
                    hasText("Group - Room", substring = true) or
                            hasText("You are not in a group")
                ).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Step 3: Check for group details or empty state
        try {
            composeTestRule.onNodeWithText("Group Members")
                .assertExists()
            // Has active group
        } catch (e: AssertionError) {
            composeTestRule.onNodeWithText("You are not in a group")
                .assertExists()
            // No active group (expected for standalone test)
        }

        // Cleanup: Go back
        composeTestRule.onNodeWithText("Go Back")
            .performClick()

        Thread.sleep(1000)
    }

    /**
     * Test Case 3.2: Vote on Restaurant - Multi-User Scenario
     *
     * REQUIRES MANUAL TESTER PARTICIPATION
     *
     * Use Case: Vote on Restaurant
     * Expected Behavior: User successfully votes for a restaurant
     *
     * MANUAL TESTER INSTRUCTIONS (User B):
     * Before running this test, a manual tester must:
     * 1. Log into the app with a DIFFERENT testing account
     * 2. Set their preferences to match User A's preferences
     * 3. Join matchmaking at approximately the same time as this test
     * 4. Wait to be matched with User A (the automated test)
     * 5. When voting screen appears, vote on any restaurant
     *
     * This test (User A) will:
     * 1. Set up preferences
     * 2. Join matchmaking
     * 3. Wait for match with User B
     * 4. Navigate to voting screen
     * 5. Select and vote on a restaurant
     * 6. Verify vote submission
     *
     * NOTE: Run this test ONLY when manual tester is ready
     */
    @Test
    fun test_09_VoteRestaurant_MultiUser() {
        // AUTOMATED SETUP
        setupPreferencesForMatchmaking()
        navigateToHome()

        println("====================================")
        println("MANUAL TESTER: Start matchmaking NOW")
        println("====================================")

        // Join matchmaking
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Wait in waiting room (allow time for manual tester to join)
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Waiting Room")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        println("Waiting for match... (up to 60 seconds)")

        // Wait for group formation (when timer expires or enough users join)
        // This transitions to voting screen automatically
        composeTestRule.waitUntil(timeoutMillis = 70000) {
            try {
                composeTestRule.onNodeWithText("Vote for Restaurant")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        println("Match found! Now on voting screen")

        // Step 1: Verify VoteRestaurantScreen is displayed
        composeTestRule.onNodeWithText("Vote for Restaurant")
            .assertExists()

        // Step 2: Wait for restaurant list to load from backend
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onAllNodesWithTag("RestaurantCard")
                    .fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // Step 3: Select first restaurant
        composeTestRule.onAllNodesWithTag("RestaurantCard")
            .onFirst()
            .performClick()

        // Step 4: Verify Submit Vote button is enabled
        composeTestRule.onNodeWithText("Submit Vote")
            .assertIsEnabled()

        // Step 5: Submit vote to REAL backend
        composeTestRule.onNodeWithText("Submit Vote")
            .performClick()

        // Step 6: Verify vote was recorded by backend
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Already Voted")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        println("Vote submitted successfully!")

        // Allow time to observe results
        Thread.sleep(5000)

        // CLEANUP: Leave group
        try {
            navigateToHome()
            composeTestRule.onNodeWithText("Current Groups")
                .performClick()
            Thread.sleep(1000)
            composeTestRule.onNodeWithText("Leave Group")
                .performClick()
            Thread.sleep(500)
            composeTestRule.onNodeWithText("Leave")
                .performClick()
        } catch (e: Exception) {
            // Group may have auto-dissolved
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Helper: Set up minimal preferences required for matchmaking
     * This is called as automated setup for tests that need it
     */
    private fun setupPreferencesForMatchmaking() {
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Preferences")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Preferences")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Japanese")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Select at least one cuisine
        composeTestRule.onNodeWithText("Japanese")
            .performClick()

        // Save to backend
        composeTestRule.onNodeWithText("Save Preferences")
            .performClick()

        // Wait for save to complete
        Thread.sleep(3000)
    }

    /**
     * Helper: Leave waiting room (cleanup)
     */
    private fun leaveWaitingRoom() {
        try {
            composeTestRule.onNodeWithText("Leave Room")
                .performClick()
            Thread.sleep(1000)
            composeTestRule.onNodeWithText("Leave")
                .performClick()
            Thread.sleep(2000)
        } catch (e: Exception) {
            // Already left or not in room
        }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension function for flexible text matching
 */
fun hasText(text: String, substring: Boolean = false, ignoreCase: Boolean = false): SemanticsMatcher {
    return SemanticsMatcher("hasText(text = '$text', substring = $substring, ignoreCase = $ignoreCase)") {
        val textValues = mutableListOf<String>()
        it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.forEach { annotatedString ->
            textValues.add(annotatedString.text)
        }
        it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.EditableText)?.let { editableText ->
            textValues.add(editableText.text)
        }

        textValues.any { value ->
            if (substring) {
                value.contains(text, ignoreCase = ignoreCase)
            } else {
                value.equals(text, ignoreCase = ignoreCase)
            }
        }
    }
}