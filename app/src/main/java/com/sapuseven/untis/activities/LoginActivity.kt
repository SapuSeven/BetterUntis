package com.sapuseven.untis.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sapuseven.untis.services.CodeScanServiceImpl
import com.sapuseven.untis.ui.activities.Login
import com.sapuseven.untis.ui.activities.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseComposeActivity() {
	private val loginViewModel: LoginViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		loginViewModel.codeScanService.setResultRegistry(activityResultRegistry) // Ugly, but I can't for the life of me make this work with Dagger/Hilt without assigning it manually.
		lifecycle.addObserver(loginViewModel.codeScanService as CodeScanServiceImpl)

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

	override fun onDestroy() {
		super.onDestroy()
		lifecycle.removeObserver(loginViewModel.codeScanService as CodeScanServiceImpl)
	}

	companion object {
		const val EXTRA_BOOLEAN_SHOW_BACK_BUTTON =
			"com.sapuseven.untis.activities.login.showBackButton"
	}
}
