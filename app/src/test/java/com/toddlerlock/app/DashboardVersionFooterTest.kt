package com.toddlerlock.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.toddlerlock.app.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DashboardVersionFooterTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboardShowsAppVersionInFooter() {
    composeTestRule.setContent { MyApplicationTheme { ToddlerLockDashboard() } }

    composeTestRule.onNodeWithText("Version 1.2.0").performScrollTo().assertIsDisplayed()
  }
}
