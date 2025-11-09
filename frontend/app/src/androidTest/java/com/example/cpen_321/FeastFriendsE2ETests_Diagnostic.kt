package com.example.cpen_321

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.cpen_321.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Diagnostic E2E Test - Minimal version to debug authentication issues
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FeastFriendsE2ETests_Diagnostic {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        println("=== TEST SETUP START ===")
        println("Test: Waiting for app to initialize...")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        println("Test: App should be initialized now")
    }

    /**
     * Diagnostic Test 1: Print the entire UI tree
     */
    @Test
    fun test_00_PrintUITree() {
        println("=== PRINTING UI TREE ===")

        // Wait a bit
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        try {
            // Print the entire semantic tree
            composeTestRule.onRoot(useUnmergedTree = true).printToLog("UI_TREE")
            println("Test: UI tree printed to logcat (search for 'UI_TREE')")
        } catch (e: Exception) {
            println("Test: ERROR printing UI tree: ${e.message}")
            e.printStackTrace()
        }

        // Also try to find common elements
        println("\n=== CHECKING FOR COMMON ELEMENTS ===")
        checkForElement("Welcome to")
        checkForElement("FeastFriends")
        checkForElement("Login")
        checkForElement("Google")
        checkForElement("Sign in")
        checkForElement("Home")
        checkForElement("Profile")
        checkForElement("Find a Match")

        println("=== UI TREE TEST COMPLETE ===")
    }

    /**
     * Diagnostic Test 2: Try to handle authentication with detailed logging
     */
    @Test
    fun test_01_HandleAuthentication() {
        println("=== AUTHENTICATION TEST START ===")
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        // Print UI tree first
        println("Test: Current UI state:")
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("AUTH_SCREEN")

        // Check for login screen
        println("\nTest: Checking for login screen...")
        val hasWelcome = hasElement("Welcome")
        val hasLogin = hasElement("Login")
        val hasGoogle = hasElement("Google")
        println("Test: Has 'Welcome': $hasWelcome")
        println("Test: Has 'Login': $hasLogin")
        println("Test: Has 'Google': $hasGoogle")

        if (hasWelcome || hasLogin) {
            println("\nTest: Appears to be on login screen")
            // Try to find and click login button
            println("Test: Looking for login button...")

            val loginButtons = listOf(
                "Login with Google",
                "Sign in with Google",
                "Login",
                "Sign In",
                "Google Sign In"
            )

            var loginClicked = false
            for (buttonText in loginButtons) {
                if (tryClick(buttonText)) {
                    println("Test: Successfully clicked '$buttonText'")
                    loginClicked = true
                    break
                }
            }

            if (loginClicked) {
                println("\nTest: Waiting for authentication...")
                Thread.sleep(10000)  // Wait 10 seconds
                composeTestRule.waitForIdle()

                println("\nTest: After authentication attempt:")
                composeTestRule.onRoot(useUnmergedTree = true).printToLog("AFTER_AUTH")

                // Check if we're on home screen now
                val hasHome = hasElement("Home")
                val hasFindMatch = hasElement("Find a Match")
                val hasGroups = hasElement("Groups")
                println("Test: Has 'Home': $hasHome")
                println("Test: Has 'Find a Match': $hasFindMatch")
                println("Test: Has 'Groups': $hasGroups")

                if (hasHome || hasFindMatch || hasGroups) {
                    println("\nTest: ✅ APPEARS TO BE AUTHENTICATED!")
                } else {
                    println("\nTest: ❌ Still not on home screen after auth attempt")
                }
            } else {
                println("\nTest: ❌ Could not find or click login button")
            }
        } else {
            println("\nTest: Not on login screen, checking for home screen...")
            val hasHome = hasElement("Home")
            val hasFindMatch = hasElement("Find a Match")

            if (hasHome || hasFindMatch) {
                println("Test: ✅ ALREADY ON HOME SCREEN!")
            } else {
                println("Test: ❌ Unknown screen state")
            }
        }
        println("=== AUTHENTICATION TEST COMPLETE ===")
    }

    /**
     * Diagnostic Test 3: Simple navigation test
     */
    @Test
    fun test_02_TryNavigation() {
        println("=== NAVIGATION TEST START ===")

        Thread.sleep(3000)
        composeTestRule.waitForIdle()

        // Print current state
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("NAV_TEST")

        // Try to find navigation elements
        println("\nTest: Looking for navigation elements...")

        val navElements = listOf(
            "Home",
            "Profile",
            "Groups",
            "Settings"
        )

        for (element in navElements) {
            val found = hasElement(element)
            println("Test: '$element' ${if (found) "FOUND ✓" else "NOT FOUND ✗"}")

            if (found) {
                println("Test: Trying to click '$element'...")
                if (tryClick(element, contentDescription = true)) {
                    println("Test: Clicked '$element', waiting...")
                    Thread.sleep(2000)
                    composeTestRule.waitForIdle()
                    composeTestRule.onRoot(useUnmergedTree = true).printToLog("AFTER_$element")
                }
            }
        }

        println("=== NAVIGATION TEST COMPLETE ===")
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun checkForElement(text: String) {
        val found = hasElement(text)
        println("Test: '$text' - ${if (found) "FOUND ✓" else "NOT FOUND ✗"}")
    }

    private fun hasElement(text: String, substring: Boolean = true): Boolean {
        return try {
            composeTestRule.onNodeWithText(text, substring = substring, useUnmergedTree = true)
                .assertExists()
            true
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithContentDescription(text, substring = substring, useUnmergedTree = true)
                    .assertExists()
                true
            } catch (e2: AssertionError) {
                false
            }
        }
    }

    private fun tryClick(text: String, contentDescription: Boolean = false, substring: Boolean = true): Boolean {
        return try {
            if (contentDescription) {
                composeTestRule.onNodeWithContentDescription(text, substring = substring, useUnmergedTree = true)
                    .performClick()
            } else {
                composeTestRule.onNodeWithText(text, substring = substring, useUnmergedTree = true)
                    .performClick()
            }
            Thread.sleep(500)
            true
        } catch (e: Exception) {
            println("Test: Could not click '$text': ${e.message}")
            false
        }
    }
}