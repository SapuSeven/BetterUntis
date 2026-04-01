package com.sapuseven.untis.feature.automute.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.sapuseven.untis.feature.automute.ui.AutoMuteSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoMuteConfigurationActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			//TODO AppTheme {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					AutoMuteSettings() { finish() }
				} else {
					Text("Auto-Mute is not supported on this device.")
				}
			//}
		}
	}
}
