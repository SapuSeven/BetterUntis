package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.pages.infocenter.InfoCenter
import com.sapuseven.untis.ui.pages.infocenter.rememberInfoCenterState

class InfoCenterActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme(navBarInset = false) {
				withUser { user ->
					val state = rememberInfoCenterState(
						userDatabase,
						user,
						//timetableDatabaseInterface,
						dataStorePreferences,
						this
					)
					InfoCenter(state)
				}
			}
		}
	}
}
