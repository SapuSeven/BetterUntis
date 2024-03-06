package com.sapuseven.untis.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.ui.activities.LoginDataInput
import com.sapuseven.untis.ui.activities.LoginDataInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi


val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(name = LoginDataInputActivity.BACKUP_PREF_NAME)

@AndroidEntryPoint
class LoginDataInputActivity : BaseComposeActivity() {
	private val loginDataInputViewModel: LoginDataInputViewModel by viewModelsWithData()

	companion object {
		const val BACKUP_PREF_NAME = "loginDataInputBackup"

		//private const val FRAGMENT_TAG_PROFILE_UPDATE = "profileUpdate"

		const val EXTRA_BOOLEAN_PROFILE_UPDATE = "com.sapuseven.untis.activities.profileupdate"
		const val EXTRA_BOOLEAN_DEMO_LOGIN = "com.sapuseven.untis.activities.demoLogin"
		const val EXTRA_STRING_SCHOOL_INFO = "com.sapuseven.untis.activities.schoolInfo"

		const val DEMO_API_URL = "https://api.sapuseven.com/untis/testing"

		val PREFS_BACKUP_SCHOOLID = stringPreferencesKey("logindatainput_backup_schoolid")
		val PREFS_BACKUP_ANONYMOUS = booleanPreferencesKey("logindatainput_backup_anonymous")
		val PREFS_BACKUP_USERNAME = stringPreferencesKey("logindatainput_backup_username")
		val PREFS_BACKUP_PASSWORD = stringPreferencesKey("logindatainput_backup_password")
		val PREFS_BACKUP_PROXYURL = stringPreferencesKey("logindatainput_backup_proxyurl")
		val PREFS_BACKUP_APIURL = stringPreferencesKey("logindatainput_backup_apiurl")
		val PREFS_BACKUP_SKIPAPPSECRET =
			booleanPreferencesKey("logindatainput_backup_skipappsecret")
	}

	private var existingUserId: Long? = null

	private var existingUser: User? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			AppTheme {
				LoginDataInput(loginDataInputViewModel)
			}
		}
	}

	@OptIn(
		ExperimentalMaterial3Api::class, ExperimentalSerializationApi::class,
		ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class
	)
	fun onCreateOld(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(
			window,
			true
		) // Workaround for bringIntoView(). Unfortunately this also breaks insets...

		getUserIdExtra(intent)?.let { userId ->
			existingUserId = userId

			existingUserId?.let { id ->
				existingUser = userDatabase.userDao().getById(id)?.also { user ->
					setUser(user)
				}
			}
		}

		setContent {
			val systemUiController = rememberSystemUiController()

			AppTheme(navBarInset = false, systemUiController = systemUiController) {
				setSystemUiColor(
					systemUiController,
					MaterialTheme.colorScheme.surface
				) // Part of the bringIntoView()-workaround - as system bars are transparent by default, set their color manually
			}
		}
	}
}

const val SAVED_STATE_INTENT_DATA = "data"

/**
 * A replacement for `viewModels()` that adds the data from the intent to the extras.
 */
@MainThread
public inline fun <reified VM : ViewModel> ComponentActivity.viewModelsWithData(
	noinline factoryProducer: (() -> Factory)? = null
): Lazy<VM> = viewModels(
	factoryProducer = factoryProducer,
	extrasProducer = {
		MutableCreationExtras(defaultViewModelCreationExtras).apply {
			set(DEFAULT_ARGS_KEY,
				(get(DEFAULT_ARGS_KEY) ?: Bundle()).apply {
					putString(
						SAVED_STATE_INTENT_DATA,
						intent.dataString
					)
				}
			)
		}
	}
)
