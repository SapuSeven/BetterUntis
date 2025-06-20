package com.sapuseven.untis.screenshots

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.ui.pages.infocenter.InfoCenter
import com.sapuseven.untis.utils.WithScreenshot
import com.sapuseven.untis.utils.takeScreenshot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class InfoCenterScreenshot {
	@get:Rule
	var rule = createAndroidComposeRule<MainActivity>()

	@get:Rule
	var hiltRule = HiltAndroidRule(this)

	@Before
	fun init() {
		hiltRule.inject()
	}

	@Test
	fun infoCenterScreenshot() {
		rule.activity.setContent {
			WithScreenshot {
				InfoCenter()
			}
		}
		rule.takeScreenshot("infocenter.png")
	}
}
