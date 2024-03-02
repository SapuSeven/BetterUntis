package com.sapuseven.untis.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sapuseven.untis.ui.activities.Login
import com.sapuseven.untis.ui.activities.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseComposeActivity() {
	private val loginViewModel: LoginViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				Login(loginViewModel) { result ->
					if (result.resultCode == Activity.RESULT_OK) {
						setResult(Activity.RESULT_OK, result.data)
						finish()
					}
				}
			}
		}
	}


	companion object {
		const val EXTRA_BOOLEAN_SHOW_BACK_BUTTON =
			"com.sapuseven.untis.activities.login.showBackButton"
	}

	/*@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		codeScanService = CodeScanServiceImpl(this, activityResultRegistry)
		lifecycle.addObserver(codeScanService as CodeScanServiceImpl)

		setContent {
			AppTheme {

			}
		}
	}*/
}
