package com.example.cpen_321.e2e.utils

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.uiautomator.UiDevice

/**
 * Test helper functions for E2E tests
 */
object TestHelpers {

    /**
     * Wait for text to appear on screen
     */
    fun ComposeTestRule.waitForText(
        text: String,
        timeoutMillis: Long = 5000,
        substring: Boolean = true
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text, substring = substring, ignoreCase = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Assert text exists on screen
     */
    fun ComposeTestRule.assertTextExists(text: String, substring: Boolean = true) {
        onNodeWithText(text, substring = substring, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    /**
     * Click button with text - UPDATED to be more robust
     */
    fun ComposeTestRule.clickButton(text: String) {
        // Wait for button to exist
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click it
        onNodeWithText(text, substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    /**
     * Grant location permission via ADB
     */
    fun UiDevice.grantLocationPermission(packageName: String) {
        executeShellCommand(
            "pm grant $packageName android.permission.ACCESS_FINE_LOCATION"
        )
        executeShellCommand(
            "pm grant $packageName android.permission.ACCESS_COARSE_LOCATION"
        )
    }

    /**
     * Revoke location permission via ADB
     */
    fun UiDevice.revokeLocationPermission(packageName: String) {
        executeShellCommand(
            "pm revoke $packageName android.permission.ACCESS_FINE_LOCATION"
        )
        executeShellCommand(
            "pm revoke $packageName android.permission.ACCESS_COARSE_LOCATION"
        )
    }

    /**
     * Enable airplane mode to simulate network error
     */
    fun UiDevice.enableAirplaneMode() {
        executeShellCommand("cmd connectivity airplane-mode enable")
        Thread.sleep(1000)
    }

    /**
     * Disable airplane mode
     */
    fun UiDevice.disableAirplaneMode() {
        executeShellCommand("cmd connectivity airplane-mode disable")
        Thread.sleep(2000)
    }

    /**
     * Wait for snackbar message
     */
    fun ComposeTestRule.waitForSnackbar(message: String, timeoutMillis: Long = 5000) {
        waitForText(message, timeoutMillis)
    }

    /**
     * Verify loading indicator exists
     */
    fun ComposeTestRule.assertLoadingExists() {
        onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate),
            useUnmergedTree = true
        ).assertExists()
    }

    /**
     * Click slider and adjust value
     */
    fun ComposeTestRule.adjustSlider(contentDescription: String, percentage: Float) {
        onNodeWithContentDescription(contentDescription, useUnmergedTree = true)
            .performTouchInput {
                val width = this.width.toFloat()
                val targetX = width * percentage
                val centerY = this.height.toFloat() / 2
                down(1, androidx.compose.ui.geometry.Offset(0f, centerY))
                moveTo(1, androidx.compose.ui.geometry.Offset(targetX, centerY))
                up(1)
            }
    }
}