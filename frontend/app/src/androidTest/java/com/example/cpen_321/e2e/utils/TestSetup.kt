package com.example.cpen_321.e2e.utils

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.*

/**
 * Common test setup and utilities for E2E tests
 */
object TestSetup {

    /**
     * Get application context
     */
    fun getContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Get instrumentation
     */
    fun getInstrumentation() = InstrumentationRegistry.getInstrumentation()

    /**
     * Get UiDevice instance
     */
    fun getDevice(): UiDevice {
        return UiDevice.getInstance(getInstrumentation())
    }

    /**
     * Package name for the app under test
     */
    const val PACKAGE_NAME = "com.example.cpen_321"

    /**
     * Default timeout for operations (milliseconds)
     */
    const val DEFAULT_TIMEOUT = 5000L

    /**
     * Long timeout for network operations (milliseconds)
     */
    const val NETWORK_TIMEOUT = 15000L

    /**
     * Timeout for waiting room operations (milliseconds)
     */
    const val WAITING_ROOM_TIMEOUT = 35000L

    /**
     * Setup function to run before each test
     */
    fun beforeEachTest(device: UiDevice) {
        // Ensure network is enabled
        device.executeShellCommand("cmd connectivity airplane-mode disable")
        Thread.sleep(2000)

        // Wake up device
        if (!device.isScreenOn) {
            device.wakeUp()
        }

        // Unlock device if locked (swipe up)
        if (!device.isScreenOn) {
            device.wakeUp()
            device.waitForIdle()
            device.swipe(
                device.displayWidth / 2,
                device.displayHeight - 100,
                device.displayWidth / 2,
                100,
                10
            )
        }

        // Wait for device to be idle
        device.waitForIdle()
    }

    /**
     * Cleanup function to run after each test
     */
    fun afterEachTest(device: UiDevice) {
        // Restore network
        device.executeShellCommand("cmd connectivity airplane-mode disable")

        // Clear any dialogs by pressing back multiple times
        repeat(3) {
            try {
                device.pressBack()
                Thread.sleep(300)
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Return to home screen
        device.pressHome()
        device.waitForIdle()
    }

    /**
     * Grant all necessary permissions for the app
     */
    fun grantAllPermissions(device: UiDevice) {
        val permissions = listOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA"
        )

        permissions.forEach { permission ->
            try {
                device.executeShellCommand("pm grant $PACKAGE_NAME $permission")
            } catch (e: Exception) {
                android.util.Log.w("TestSetup", "Could not grant permission: $permission")
            }
        }

        Thread.sleep(1000)
    }

    /**
     * Revoke all permissions for the app
     */
    fun revokeAllPermissions(device: UiDevice) {
        val permissions = listOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA"
        )

        permissions.forEach { permission ->
            try {
                device.executeShellCommand("pm revoke $PACKAGE_NAME $permission")
            } catch (e: Exception) {
                android.util.Log.w("TestSetup", "Could not revoke permission: $permission")
            }
        }

        Thread.sleep(1000)
    }

    /**
     * Clear app data (use with caution - will reset app state)
     */
    fun clearAppData(device: UiDevice) {
        device.executeShellCommand("pm clear $PACKAGE_NAME")
        Thread.sleep(2000)
    }

    /**
     * Force stop the app
     */
    fun forceStopApp(device: UiDevice) {
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        Thread.sleep(1000)
    }

    /**
     * Launch the app
     */
    fun launchApp(device: UiDevice) {
        val context = getContext()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(
            androidx.test.uiautomator.Until.hasObject(
                androidx.test.uiautomator.By.pkg(PACKAGE_NAME)
            ),
            DEFAULT_TIMEOUT
        )
    }

    /**
     * Take a screenshot (for debugging failed tests)
     */
    fun takeScreenshot(device: UiDevice, testName: String) {
        val screenshotFile = java.io.File(
            getContext().getExternalFilesDir(null),
            "screenshots/$testName-${System.currentTimeMillis()}.png"
        )
        screenshotFile.parentFile?.mkdirs()
        device.takeScreenshot(screenshotFile)
        android.util.Log.d("TestSetup", "Screenshot saved: ${screenshotFile.absolutePath}")
    }

    /**
     * Check if app is in foreground
     */
    fun isAppInForeground(device: UiDevice): Boolean {
        return device.currentPackageName == PACKAGE_NAME
    }

    /**
     * Wait for app to be in foreground
     */
    fun waitForAppInForeground(device: UiDevice, timeoutMillis: Long = DEFAULT_TIMEOUT): Boolean {
        return device.wait(
            androidx.test.uiautomator.Until.hasObject(
                androidx.test.uiautomator.By.pkg(PACKAGE_NAME)
            ),
            timeoutMillis
        )
    }


    /**
     * Check if user is authenticated
     * @return true if on home screen (authenticated), false if on auth screen
     */
    fun isUserAuthenticated(composeTestRule: ComposeTestRule): Boolean {
        return try {
            // Wait briefly for screen to load
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule.onAllNodesWithText("Start Matchmaking", substring = true)
                    .fetchSemanticsNodes().isNotEmpty() ||
                        composeTestRule.onAllNodesWithText("Welcome to", substring = true)
                            .fetchSemanticsNodes().isNotEmpty()
            }

            // Check if we're on home screen
            composeTestRule.onAllNodesWithText("Start Matchmaking", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Skip test if user is not authenticated
     */
    fun requireAuthentication(composeTestRule: ComposeTestRule) {
        val isAuthenticated = isUserAuthenticated(composeTestRule)
        org.junit.Assume.assumeTrue(
            """
            ❌ TEST SKIPPED: User not authenticated
            
            To run this test:
            1. Run: ./gradlew installDebug
            2. Open app on emulator
            3. Login with Google
            4. Keep app installed
            5. Run: ./gradlew connectedDebugAndroidTest
            """.trimIndent(),
            isAuthenticated
        )
    }
}