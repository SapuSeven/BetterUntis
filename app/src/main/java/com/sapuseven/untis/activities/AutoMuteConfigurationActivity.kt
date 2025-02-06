package com.sapuseven.untis.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.glance.text.Text
import com.sapuseven.untis.helpers.AppTheme
import com.sapuseven.untis.ui.pages.settings.automute.AutoMuteSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoMuteConfigurationActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					AutoMuteSettings() { finish() }
				} else {
					Text("Auto-Mute is not supported on this device.")
				}
			}
		}
	}
}
