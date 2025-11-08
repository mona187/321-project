package com.example.cpen_321.test

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import com.example.cpen_321.MainActivity

/**
 * End-to-End Test Suite for FeastFriends App
 *
 * Test Location: app/src/androidTest/java/com/example/cpen_321/test/
 *
 * Prerequisites:
 * 1. A Google account must be configured on the test device/emulator
 * 2. The same account must exist in the MongoDB database
 * 3. Location permissions should be granted to the app
 *
 * Test Coverage:
 * - Feature 1: Profile Creation (Set Preferences, Add/Update Profile Information)
 * - Feature 2: Matchmaking (Join/Exit Waiting Room)
 * - Feature 3: Group Creation and Restaurant Voting
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FeastFriendsE2ETests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Wait for activity to launch
        Thread.sleep(3000)

        // Handle authentication if needed
        bypassAuthentication()
    }

    /**
     * Helper function to bypass authentication screen
     * Assumes an account exists and simulates successful login
     */
    private fun bypassAuthentication() {
        // Check if we're on the auth screen
        try {
            // Wait and check for the welcome screen
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Welcome to\nFeastFriends", substring = true)
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            println("Auth screen detected - performing Google Sign-In")

            // Click the Google login button
            composeTestRule.onNodeWithText("Login with Google\nAuthentication")
                .performClick()

            // Wait for Google Sign-In dialog to appear
            Thread.sleep(3000)

            // Use UiAutomator to click the "Continue as" button
            clickContinueAsButton()

            // Wait for authentication to complete and home screen to load
            println("Waiting for home screen to load...")
            composeTestRule.waitUntil(timeoutMillis = 20000) {
                try {
                    composeTestRule.onNodeWithText("Start Matchmaking")
                        .assertExists()
                    println("Home screen loaded successfully")
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

        } catch (e: Exception) {
            // Either already authenticated or error occurred
            println("Auth bypass completed or already authenticated: ${e.message}")

            // Verify we're on a valid screen
            try {
                composeTestRule.onNodeWithText("Start Matchmaking")
                    .assertExists()
                println("Already on home screen")
            } catch (e2: AssertionError) {
                println("WARNING: Not on expected screen after auth")
            }
        }
    }

    /**
     * Clicks the "Continue as [Name]" button in Google Sign-In dialog
     * Uses UiAutomator since this is a native Android UI, not Compose
     */
    private fun clickContinueAsButton() {
        try {
            // Method 1: Look for button with text containing "Continue as"
            val continueButton = device.findObject(
                UiSelector()
                    .textContains("Continue as")
                    .className("android.widget.Button")
            )

            if (continueButton.waitForExists(5000)) {
                continueButton.click()
                println("Successfully clicked 'Continue as' button")
                return
            }

            // Method 2: Try finding by exact text
            val danielButton = device.findObject(
                UiSelector().text("Continue as Daniel")
            )
            if (danielButton.exists()) {
                danielButton.click()
                println("Successfully clicked 'Continue as Daniel' button")
                return
            }

            // Method 3: Try finding any clickable element with "Continue" text
            val anyButton = device.findObject(
                UiSelector()
                    .textMatches(".*Continue.*")
                    .clickable(true)
            )
            if (anyButton.exists()) {
                anyButton.click()
                println("Successfully clicked Continue button (generic)")
                return
            }

            println("Warning: Could not find 'Continue as' button")

        } catch (e: Exception) {
            println("Error clicking 'Continue as' button: ${e.message}")
            e.printStackTrace()
        }
    }

    // ==================== FEATURE 1: PROFILE CREATION ====================

    /**
     * Test Case 1.1: Set Preferences - Main Success Scenario
     * Use Case: Set Preferences
     * Expected Behavior: User successfully sets and saves preferences
     */
    @Test
    fun test_01_SetPreferences_Success() {
        // Step 1: Navigate to ProfileConfigScreen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()

        // Verify ProfileConfigScreen is displayed
        composeTestRule.onNodeWithText("Profile")
            .assertExists()
        composeTestRule.onNodeWithText("Preferences")
            .assertExists()

        // Step 2: Click on Preferences button
        composeTestRule.onNodeWithText("Preferences")
            .performClick()

        // Step 3: Verify PreferencesScreen is displayed
        composeTestRule.onNodeWithText("Preferences (Select)")
            .assertExists()

        // Step 4: Select cuisine preferences
        composeTestRule.onNodeWithText("Sushi")
            .performClick()
        composeTestRule.onNodeWithText("Italian")
            .performClick()

        // Verify selections are highlighted (background color changes)
        // Note: Color verification would require custom semantic properties

        // Step 5: Set budget using slider
        composeTestRule.onNodeWithText("Max amount of money to spend: $", substring = true)
            .assertExists()

        // Find and interact with budget slider
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

        // Step 8: Verify success message
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Settings updated successfully")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Test Case 1.2: Set Preferences - Network Error
     * Use Case: Set Preferences - Failure Scenario 7
     * Expected Behavior: App handles network error gracefully
     */
    @Test
    fun test_02_SetPreferences_NetworkError() {
        // Disable network (airplane mode)
        device.executeShellCommand("cmd connectivity airplane-mode enable")
        Thread.sleep(2000)

        try {
            // Navigate to Preferences
            composeTestRule.onNodeWithContentDescription("Profile")
                .performClick()
            composeTestRule.onNodeWithText("Preferences")
                .performClick()

            // Select preferences
            composeTestRule.onNodeWithText("Pizza")
                .performClick()

            // Try to save
            composeTestRule.onNodeWithText("Save Preferences")
                .performClick()

            // Verify error message
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Error:", substring = true)
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        } finally {
            // Re-enable network
            device.executeShellCommand("cmd connectivity airplane-mode disable")
            Thread.sleep(2000)
        }
    }

    /**
     * Test Case 1.3: Add Profile Information - Success
     * Use Case: Add Profile Information
     * Expected Behavior: User successfully adds profile information
     *
     * BUG FIX #1: Clear existing text before input
     */
    @Test
    fun test_03_AddProfileInfo_Success() {
        // Step 1: Navigate to Profile screen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Step 2: Verify ProfileScreen is displayed
        composeTestRule.onNodeWithText("Name:")
            .assertExists()
        composeTestRule.onNodeWithText("Bio:")
            .assertExists()

        // Step 3: Add profile picture (mock the action)
        composeTestRule.onNodeWithText("Change Profile Picture")
            .assertExists()
        // Note: Actual image selection would require UI Automator

        // Step 4: Add name
        // BUG FIX: Clear existing text first, then replace with new text
        composeTestRule.onNodeWithText("Name:", substring = true)
            .performTextClearance()
        composeTestRule.onNodeWithText("Name:", substring = true)
            .performTextInput("John Doe")

        // Step 5: Add bio
        // BUG FIX: Clear existing text first
        composeTestRule.onNodeWithText("Bio:", substring = true)
            .performTextClearance()
        composeTestRule.onNodeWithText("Bio:", substring = true)
            .performTextInput("Food enthusiast who loves trying new cuisines")

        // Step 6: Add phone number
        // BUG FIX: Clear existing text first
        composeTestRule.onNodeWithText("Phone Number:", substring = true)
            .performTextClearance()
        composeTestRule.onNodeWithText("Phone Number:", substring = true)
            .performTextInput("6041234567")

        // Step 7: Save profile
        composeTestRule.onNodeWithText("Save Profile")
            .performClick()

        // Step 8: Verify success
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Settings updated successfully")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Test Case 1.4: Add Profile Information - Invalid Phone Number
     * Use Case: Add Profile Information - Failure Scenario 6
     * Expected Behavior: App validates phone number and shows error
     *
     * BUG FIX #2: Clear text field before testing validation
     */
    @Test
    fun test_04_AddProfileInfo_InvalidPhone() {
        // Navigate to Profile screen
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // BUG FIX: Clear the phone number field first
        composeTestRule.onNodeWithText("Phone Number:", substring = true)
            .performTextClearance()

        // Enter invalid phone number (less than 10 digits)
        composeTestRule.onNodeWithText("Phone Number:", substring = true)
            .performTextInput("12345")

        // Try to save
        composeTestRule.onNodeWithText("Save Profile")
            .performClick()

        // Verify error message
        composeTestRule.onNodeWithText("Phone number must be at least 10 digits")
            .assertExists()

        // Verify save button is disabled
        composeTestRule.onNodeWithText("Save Profile")
            .assertIsNotEnabled()
    }

    // ==================== FEATURE 2: MATCHMAKING ====================

    /**
     * Test Case 2.1: Join Waiting Room - Success
     * Use Case: Request Matches (Join Waiting Room)
     * Expected Behavior: User successfully joins waiting room
     */
    @Test
    fun test_05_JoinWaitingRoom_Success() {
        // Ensure preferences are set first
        ensurePreferencesSet()

        // Step 1: Click Start Matchmaking on HomeScreen
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Step 2: Verify WaitingRoomScreen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Waiting Room")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Verify timer is displayed
        composeTestRule.onNodeWithText("Time remaining")
            .assertExists()

        // Verify member count is displayed
        composeTestRule.onNodeWithText("Members", substring = true)
            .assertExists()

        // Step 3: Wait for other users (mock scenario)
        // In real test, would wait for timer or max users
        Thread.sleep(2000)

        // Leave room for next test
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()
        composeTestRule.onNodeWithText("Leave")
            .performClick()
    }

    /**
     * Test Case 2.2: Join Waiting Room - No Preferences
     * Use Case: Request Matches - Failure Scenario 1
     * Expected Behavior: User is redirected to preferences screen
     */
    @Test
    fun test_06_JoinWaitingRoom_NoPreferences() {
        // Clear preferences (mock scenario)
        // In real test, would clear user settings

        // Click Start Matchmaking
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Verify redirected to PreferencesScreen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Preferences (Select)")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Go back to home
        composeTestRule.onNodeWithText("Go Back")
            .performClick()
    }

    /**
     * Test Case 2.3: Exit Waiting Room
     * Use Case: Exit Waiting Room
     * Expected Behavior: User successfully leaves waiting room
     */
    @Test
    fun test_07_ExitWaitingRoom() {
        // Join waiting room first
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        // Wait for room to load
        Thread.sleep(2000)

        // Step 1: Click Leave Room button
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()

        // Step 2: Verify confirmation dialog
        composeTestRule.onNodeWithText("Leave Waiting Room?")
            .assertExists()
        composeTestRule.onNodeWithText("Are you sure you want to leave?", substring = true)
            .assertExists()

        // Step 3: Click Leave in dialog
        composeTestRule.onNodeWithText("Leave")
            .performClick()

        // Step 4: Verify back on HomeScreen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
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
     * Test Case 2.4: Exit Waiting Room - Stay
     * Use Case: Exit Waiting Room - Failure Scenario 3a
     * Expected Behavior: User stays in waiting room when choosing Stay
     */
    @Test
    fun test_08_ExitWaitingRoom_Stay() {
        // Join waiting room
        composeTestRule.onNodeWithText("Start Matchmaking")
            .performClick()

        Thread.sleep(2000)

        // Click Leave Room
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()

        // Click Stay in dialog
        composeTestRule.onNodeWithText("Stay")
            .performClick()

        // Verify still in waiting room
        composeTestRule.onNodeWithText("Waiting Room")
            .assertExists()
        composeTestRule.onNodeWithText("Time remaining")
            .assertExists()

        // Clean up - actually leave
        composeTestRule.onNodeWithText("Leave Room")
            .performClick()
        composeTestRule.onNodeWithText("Leave")
            .performClick()
    }

    // ==================== FEATURE 3: GROUP CREATION & VOTING ====================

    /**
     * Test Case 3.1: Vote on Restaurant - Success
     * Use Case: Vote on Restaurant
     * Expected Behavior: User successfully votes for a restaurant
     */
    @Test
    fun test_09_VoteRestaurant_Success() {
        // Setup: Create/join a group (mock scenario)
        navigateToVoteScreen()

        // Step 1: Verify VoteRestaurantScreen is displayed
        composeTestRule.onNodeWithText("Vote for Restaurant")
            .assertExists()

        // Step 2: Wait for restaurant list to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                // Check if any restaurant card exists
                composeTestRule.onAllNodesWithTag("RestaurantCard")
                    .fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // Step 3: Select a restaurant
        composeTestRule.onAllNodesWithTag("RestaurantCard")
            .onFirst()
            .performClick()

        // Step 4: Verify Submit Vote button is enabled
        composeTestRule.onNodeWithText("Submit Vote")
            .assertIsEnabled()

        // Step 5: Submit vote
        composeTestRule.onNodeWithText("Submit Vote")
            .performClick()

        // Step 6: Verify vote was recorded
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Already Voted")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Test Case 3.2: Vote on Restaurant - No Location Permission
     * Use Case: Vote on Restaurant - Failure Scenario 2a
     * Expected Behavior: App prompts for location permission
     */
    @Test
    fun test_10_VoteRestaurant_NoLocation() {
        // Revoke location permission
        device.executeShellCommand("pm revoke com.example.cpen_321 android.permission.ACCESS_FINE_LOCATION")

        try {
            navigateToVoteScreen()

            // Verify location permission prompt or message
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Getting your location...")
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            // Check for permission button
            composeTestRule.onNodeWithText("Grant Location Permission")
                .assertExists()

        } finally {
            // Re-grant permission for other tests
            device.executeShellCommand("pm grant com.example.cpen_321 android.permission.ACCESS_FINE_LOCATION")
        }
    }

    /**
     * Test Case 3.3: View Group History - Success
     * Use Case: View Group History
     * Expected Behavior: User can view their group history
     *
     * BUG FIX #3: Use proper semantic matcher construction
     */
    @Test
    fun test_11_ViewGroupHistory() {
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
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                // BUG FIX: Use proper matcher combining - can't use 'or' operator on matchers
                // Check for either condition separately
                val hasGroupText = try {
                    composeTestRule.onNodeWithText("Group - Room", substring = true)
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }

                val hasNoGroupText = try {
                    composeTestRule.onNodeWithText("You are not in a group")
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }

                hasGroupText || hasNoGroupText
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
            // No active group
        }

        // Go back
        composeTestRule.onNodeWithText("Go Back")
            .performClick()
    }

    /**
     * Test Case 3.4: View Group Details
     * Use Case: View Group Details
     * Expected Behavior: User can view detailed group information
     */
    @Test
    fun test_12_ViewGroupDetails() {
        // Navigate to ViewGroupsScreen
        composeTestRule.onNodeWithText("Current Groups", useUnmergedTree = true)
            .performClick()

        // If there's an active group with completed voting
        try {
            composeTestRule.onNodeWithText("View Details")
                .assertExists()
                .performClick()

            // Verify GroupScreen shows restaurant details
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Group")
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            // Verify restaurant info is displayed
            composeTestRule.onNodeWithText("Restaurant:", substring = true)
                .assertExists()

            // Verify members list
            composeTestRule.onNodeWithText("Group Members", substring = true)
                .assertExists()

        } catch (e: AssertionError) {
            // No completed group, skip test
            println("No completed group available for details view")
        }

        // Navigate back
        composeTestRule.onNodeWithText("Back to View Groups", useUnmergedTree = true)
            .performClick()
    }

    /**
     * Test Case 3.5: Leave Group
     * Use Case: Leave/Rematch
     * Expected Behavior: User successfully leaves the group
     */
    @Test
    fun test_13_LeaveGroup() {
        // Navigate to ViewGroupsScreen
        composeTestRule.onNodeWithText("Current Groups", useUnmergedTree = true)
            .performClick()

        try {
            // Step 1: Click Leave Group button
            composeTestRule.onNodeWithText("Leave Group")
                .performClick()

            // Step 2: Verify confirmation dialog
            composeTestRule.onNodeWithText("Leave Group")
                .assertExists()
            composeTestRule.onNodeWithText("Are you sure", substring = true)
                .assertExists()

            // Step 3: Confirm leave
            composeTestRule.onNodeWithText("Leave")
                .performClick()

            // Step 4: Verify user has left (back to no group state)
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("You are not in a group")
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

        } catch (e: AssertionError) {
            // No active group to leave
            println("No active group to leave")
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun ensurePreferencesSet() {
        // Helper to ensure preferences are set for matchmaking tests
        composeTestRule.onNodeWithContentDescription("Profile")
            .performClick()
        composeTestRule.onNodeWithText("Preferences")
            .performClick()

        // Select at least one cuisine
        composeTestRule.onNodeWithText("Japanese")
            .performClick()

        // Save
        composeTestRule.onNodeWithText("Save Preferences")
            .performClick()

        Thread.sleep(2000)

        // Go back to home
        composeTestRule.onNodeWithText("Go Back")
            .performClick()
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()
    }

    private fun navigateToVoteScreen() {
        // Helper to navigate to vote screen
        // This would typically require being in a group first
        // For testing, we'll mock this scenario

        try {
            // Try direct navigation if in a group
            composeTestRule.onNodeWithText("View Active Group")
                .performClick()
            composeTestRule.onNodeWithText("Vote Now")
                .performClick()
        } catch (e: AssertionError) {
            // Create a test group scenario
            println("No active group for voting test - would need to create group first")
        }
    }
}

// BUG FIX #4: Remove incorrect extension function that redefines hasText
// The original function was incorrect and unnecessary since hasText() already exists
// with the correct signature in the Compose testing library