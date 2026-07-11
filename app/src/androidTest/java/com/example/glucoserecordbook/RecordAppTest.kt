package com.example.glucoserecordbook


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class RecordAppTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun emptyCategoryCanBeFilledWithTheLargeKeypad() {
        composeRule.onAllNodesWithText("+ ADD READING")[0].performClick()
        composeRule.onNodeWithText("1").performClick()
        composeRule.onNodeWithText("2").performClick()
        composeRule.onNodeWithText("3").performClick()
        composeRule.onNodeWithText("SAVE").performClick()
        composeRule.onNodeWithText("123").assertExists()
    }
}
