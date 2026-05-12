package com.example.hasiru

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.hasiru", appContext.packageName)
    }

    @Test
    fun testAppNameIsCorrect() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = appContext.getString(R.string.app_name)
        assertEquals("Hasiru", appName)
    }

    @Test
    fun testInitialScreenDisplay() {
        // The app starts with a SplashScreen (3s delay). 
        // composeTestRule automatically waits for the app to be idle.
        
        // We check for text that appears on the Welcome screen (if not logged in)
        // or the Home screen (if logged in).
        
        // First, we'll try to wait for the Splash screen to finish by waiting for 
        // a known element from either the Welcome or Home screen.
        
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasText("Continue with Email")).fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodes(hasText("Namma Hasiru")).fetchSemanticsNodes().isNotEmpty()
        }

        // Now assert that at least one of them is displayed.
        val welcomeNode = composeTestRule.onAllNodes(hasText("Continue with Email"))
        val homeNode = composeTestRule.onAllNodes(hasText("Namma Hasiru"))
        
        val isWelcomeDisplayed = welcomeNode.fetchSemanticsNodes().isNotEmpty()
        val isHomeDisplayed = homeNode.fetchSemanticsNodes().isNotEmpty()
        
        assertTrue("Neither Welcome Screen nor Home Screen is displayed", isWelcomeDisplayed || isHomeDisplayed)
    }
}
