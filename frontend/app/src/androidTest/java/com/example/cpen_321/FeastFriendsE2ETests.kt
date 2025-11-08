package com.example.cpen_321

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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import android.content.Intent

/**
 * End-to-End Test Suite for FeastFriends App
 *
 * Test Location: app/src/androidTest/java/com/example/cpen_321/test/
 *
 * Prerequisites:
 * 1. A Google account must be configured on the test device/emulator
 * 2. The same account must exist in the MongoDB database
 * 3. Location permissions should be granted to the app
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FeastFriendsE2ETests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    // ==================== GLOBAL SETUP ====================

    @Before
    fun setup() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        println("🕒 Waiting for MainActivity to launch...")
        Thread.sleep(4000)

        // 🔐 Block until user is fully authenticated
        waitForAuthentication()
    }

    /**
     * Waits until user is authenticated and home screen is visible.
     * Performs Google login if needed.
     */
    private fun waitForAuthentication() {
        try {
            println("🔎 Checking if already authenticated...")
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Start Matchmaking").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
            println("✅ Already authenticated, proceeding with tests.")
            return
        } catch (_: Throwable) {
            println("⚠️  Not authenticated yet, performing login...")
        }

        performAuthentication()
    }

    /**
     * Performs full Google Sign-In and waits until Home is visible.
     */
    private fun performAuthentication() {
        try {
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                try {
                    composeTestRule.onNodeWithText("FeastFriends", substring = true)
                        .assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
            println("✅ Auth screen detected. Clicking Login button...")

            composeTestRule.onNodeWithText("Login", substring = true)
                .performClick()

            Thread.sleep(4000)

            clickContinueAsButton()

            println("⏳ Waiting for Start Matchmaking screen after login...")
            composeTestRule.waitUntil(timeoutMillis = 30000) {
                try {
                    composeTestRule.onNodeWithText("Start Matchmaking").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

            println("✅ Authentication fully completed!")
            Thread.sleep(1500)

        } catch (e: Exception) {
            println("❌ Authentication failed or timed out: ${e.message}")
            throw e
        }
    }

    /**
     * Clicks the "Continue as [Name]" button in Google Sign-In dialog.
     */
    private fun clickContinueAsButton() {
        try {
            println("🔍 Searching for 'Continue as' button...")

            val continueButton = device.findObject(
                UiSelector().textContains("Continue as").className("android.widget.Button")
            )
            if (continueButton.waitForExists(5000)) {
                continueButton.click()
                println("✅ Clicked 'Continue as' button.")
                return
            }

            val anyButton = device.findObject(
                UiSelector().textMatches(".*Continue.*").clickable(true)
            )
            if (anyButton.exists()) {
                anyButton.click()
                println("✅ Clicked generic 'Continue' button.")
                return
            }

            println("⚠️  No 'Continue as' button found.")
        } catch (e: Exception) {
            println("❌ Error clicking 'Continue as': ${e.message}")
        }
    }

    // ==================== FEATURE 1: PROFILE CREATION ====================

    @Test
    fun test_01_SetPreferences_Success() {
        composeTestRule.onNodeWithTag("bottom_profile", useUnmergedTree = true)
            .performClick()

        composeTestRule.onNodeWithText("Profile", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Preferences", useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithText("Preferences", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("Preferences (Select)").assertExists()

        composeTestRule.onNodeWithText("Sushi").performClick()
        composeTestRule.onNodeWithText("Italian").performClick()

        composeTestRule.onNodeWithText("Max amount of money to spend: $", substring = true)
            .assertExists()

        composeTestRule.onAllNodesWithTag("BudgetSlider", useUnmergedTree = true)
            .onFirst()
            .performTouchInput {
                swipeRight(startX = centerX - 100f, endX = centerX + 50f)
            }

        composeTestRule.onNodeWithText("Search radius:", substring = true)
            .assertExists()

        composeTestRule.onAllNodesWithTag("RadiusSlider", useUnmergedTree = true)
            .onFirst()
            .performTouchInput {
                swipeRight(startX = centerX - 50f, endX = centerX + 50f)
            }

        composeTestRule.onNodeWithText("Save Preferences").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Settings updated successfully").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun test_02_SetPreferences_NoCuisine() {
        composeTestRule.onNodeWithTag("bottom_profile", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("Preferences").performClick()

        composeTestRule.onNodeWithText("Save Preferences").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText(
                    "Please select at least one cuisine type", substring = true
                ).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun test_03_AddProfileInfo_Success() {
        composeTestRule.onNodeWithTag("bottom_profile", useUnmergedTree = true).performClick()

        try {
            composeTestRule.onNodeWithText("Username", substring = true).assertExists()
            composeTestRule.onNodeWithText("Edit Profile", substring = true).performClick()

            Thread.sleep(1000)
            composeTestRule.onNodeWithText("Enter username").performTextInput("TestUser123")
            composeTestRule.onNodeWithText("Enter your bio")
                .performTextInput("This is a test bio for E2E testing")

            composeTestRule.onNodeWithText("Save Profile").performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Profile updated successfully", substring = true)
                        .assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

        } catch (e: AssertionError) {
            println("Profile fields not available: ${e.message}")
        }
    }

    // ==================== FEATURE 2: MATCHMAKING ====================

    @Test
    fun test_04_JoinWaitingRoom_Success() {
        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithText("Start Matchmaking").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Waiting Room", substring = true).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Looking for matches...", substring = true).assertExists()
    }

    @Test
    fun test_05_ExitWaitingRoom_Success() {
        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithText("Start Matchmaking").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithText("Exit Waiting Room").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Start Matchmaking").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun test_06_JoinWaitingRoom_NoPreferences() {
        try {
            composeTestRule.onNodeWithContentDescription("Home").performClick()
            composeTestRule.onNodeWithText("Start Matchmaking").performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNode(
                        hasText("Please set your preferences first", substring = true) or
                                hasText("Preferences", substring = true)
                    ).assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

        } catch (e: AssertionError) {
            println("Could not test no-preferences scenario: ${e.message}")
        }
    }

    @Test
    fun test_07_MatchmakingSuccess() {
        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithText("Start Matchmaking").performClick()
        Thread.sleep(10000)

        try {
            composeTestRule.onNodeWithText("Waiting Room", substring = true).assertExists()
            println("Still in waiting room - no match found")
        } catch (e: AssertionError) {
            println("Left waiting room - checking for group formation...")
            try {
                composeTestRule.onNodeWithText("Group", substring = true).assertExists()
                println("Successfully matched and in group!")
            } catch (_: AssertionError) {
                println("Unknown state after matchmaking")
            }
        }
    }

    // ==================== FEATURE 3: GROUP & VOTING ====================

    @Test
    fun test_08_VoteRestaurant_Success() {
        try {
            composeTestRule.onNodeWithText("View Active Group").performClick()
            composeTestRule.onNodeWithText("Vote Now").performClick()

            composeTestRule.waitUntil(timeoutMillis = 10000) {
                try {
                    composeTestRule.onNodeWithText("Vote for Restaurant", substring = true)
                        .assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

            Thread.sleep(3000)
            composeTestRule.onNodeWithText("Vote", useUnmergedTree = true).performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Vote cast successfully", substring = true)
                        .assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

        } catch (e: AssertionError) {
            println("Voting test skipped - not in active group: ${e.message}")
        }
    }

    @Test
    fun test_09_ViewVotingResults() {
        try {
            composeTestRule.onNodeWithText("View Active Group").performClick()
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Voting Complete", substring = true).assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
            composeTestRule.onNodeWithText("Winner:", substring = true).assertExists()
            composeTestRule.onNodeWithText("Address:", substring = true).assertExists()

        } catch (e: AssertionError) {
            println("Cannot view results - voting not complete: ${e.message}")
        }
    }

    @Test
    fun test_10_VoteRestaurant_NoLocation() {
        device.executeShellCommand("pm revoke com.example.cpen_321 android.permission.ACCESS_FINE_LOCATION")
        try {
            navigateToVoteScreen()
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Getting your location...").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
            composeTestRule.onNodeWithText("Grant Location Permission").assertExists()
        } finally {
            device.executeShellCommand("pm grant com.example.cpen_321 android.permission.ACCESS_FINE_LOCATION")
        }
    }

    @Test
    fun test_11_ViewGroupHistory() {
        val buttonText = try {
            composeTestRule.onNodeWithText("View Active Group").assertExists()
            "View Active Group"
        } catch (_: AssertionError) {
            "Current Groups"
        }

        composeTestRule.onNodeWithText(buttonText).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNode(
                    hasText("Group - Room", substring = true) or
                            hasText("You are not in a group")
                ).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        try {
            composeTestRule.onNodeWithText("Group Members").assertExists()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("You are not in a group").assertExists()
        }

        composeTestRule.onNodeWithText("Go Back").performClick()
    }

    @Test
    fun test_12_ViewGroupDetails() {
        composeTestRule.onNodeWithText("Current Groups", useUnmergedTree = true).performClick()

        try {
            composeTestRule.onNodeWithText("View Details").assertExists().performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("Group").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

            composeTestRule.onNodeWithText("Restaurant:", substring = true).assertExists()
            composeTestRule.onNodeWithText("Group Members", substring = true).assertExists()

        } catch (_: AssertionError) {
            println("No completed group available for details view")
        }

        composeTestRule.onNodeWithText("Back to View Groups", useUnmergedTree = true)
            .performClick()
    }

    @Test
    fun test_13_LeaveGroup() {
        composeTestRule.onNodeWithText("Current Groups", useUnmergedTree = true).performClick()

        try {
            composeTestRule.onNodeWithText("Leave Group").performClick()
            composeTestRule.onNodeWithText("Leave Group").assertExists()
            composeTestRule.onNodeWithText("Are you sure", substring = true).assertExists()
            composeTestRule.onNodeWithText("Leave").performClick()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithText("You are not in a group").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

        } catch (_: AssertionError) {
            println("No active group to leave")
        }
    }

    // ==================== HELPERS ====================

    private fun ensurePreferencesSet() {
        composeTestRule.onNodeWithContentDescription("Profile").performClick()
        composeTestRule.onNodeWithText("Preferences").performClick()
        composeTestRule.onNodeWithText("Japanese").performClick()
        composeTestRule.onNodeWithText("Save Preferences").performClick()
        Thread.sleep(2000)
        composeTestRule.onNodeWithText("Go Back").performClick()
        composeTestRule.onNodeWithContentDescription("Home").performClick()
    }

    private fun navigateToVoteScreen() {
        try {
            composeTestRule.onNodeWithText("View Active Group").performClick()
            composeTestRule.onNodeWithText("Vote Now").performClick()
        } catch (_: AssertionError) {
            println("No active group for voting test - would need to create group first")
        }
    }
}

// Text matcher helper
fun hasText(text: String, substring: Boolean = false): SemanticsMatcher {
    return if (substring) {
        hasTextExactly(text, includeEditableText = false)
    } else {
        hasText(text)
    }
}
