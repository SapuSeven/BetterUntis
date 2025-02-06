package com.sapuseven.untis.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import com.sapuseven.untis.ui.pages.settings.automute.AutoMuteSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoMuteConfigurationActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			MaterialTheme(
				colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					if (isSystemInDarkTheme()) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
				} else {
					if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
				}
			) {
				AutoMuteSettings() { finish() }
			}
		}
	}
}
