package com.sapuseven.untis.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInput
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ExperimentalSerializationApi

@AndroidEntryPoint
class LoginDataInputActivity : BaseComposeActivityNew<LoginDataInputViewModel>() {
	override val viewModel: LoginDataInputViewModel by viewModelsWithData()

	companion object {
		const val BACKUP_PREF_NAME = "loginDataInputBackup"

		const val DEMO_API_URL = "https://api.sapuseven.com/untis/testing"
	}

	private var existingUserId: Long? = null

	private var existingUser: User? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}
}
