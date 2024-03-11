package com.sapuseven.untis.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sapuseven.untis.services.CodeScanServiceImpl
import com.sapuseven.untis.ui.activities.login.Login
import com.sapuseven.untis.ui.activities.login.LoginViewModel
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInputViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseComposeActivityNew<LoginViewModel>() {
	override val viewModel: LoginViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel.codeScanService.setResultRegistry(activityResultRegistry) // Ugly, but I can't for the life of me make this work with Dagger/Hilt without assigning it manually.
		lifecycle.addObserver(viewModel.codeScanService as CodeScanServiceImpl)
	}

	override fun onDestroy() {
		super.onDestroy()
		lifecycle.removeObserver(viewModel.codeScanService as CodeScanServiceImpl)
	}

	companion object {
		const val EXTRA_BOOLEAN_SHOW_BACK_BUTTON =
			"com.sapuseven.untis.activities.login.showBackButton"
	}
}
