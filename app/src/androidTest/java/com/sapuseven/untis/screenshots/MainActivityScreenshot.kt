package com.sapuseven.untis.screenshots

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.utils.takeScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityScreenshot {
	@get:Rule
	val rule = createAndroidComposeRule<MainActivity>()

	@Test
	fun mainActivityScreenshot() {
		rule.takeScreenshot("activity-main.png")
	}
}
