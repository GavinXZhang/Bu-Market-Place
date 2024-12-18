package com.example.bumarketplace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class BuMarketPlaceTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test for LoginScreen
    @Test
    fun testLoginScreenDisplaysCorrectText() {
        composeTestRule.setContent {
            LoginScreen(onGoogleSignInClicked = {})
        }

        // Check if the main text is displayed
        composeTestRule.onNodeWithText("Discover BUMarket").assertIsDisplayed()

        // Check if the subtitle is displayed
        composeTestRule.onNodeWithText("Connect with BU students to buy and sell").assertIsDisplayed()

        // Simulate clicking the button
        composeTestRule.onNodeWithText("Sign-Up with BU Google").performClick()
    }

}
