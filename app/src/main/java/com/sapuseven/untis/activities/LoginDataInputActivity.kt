package com.sapuseven.untis.activities

import android.os.Bundle
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.ui.pages.login.datainput.LoginDataInputViewModel
import dagger.hilt.android.AndroidEntryPoint

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
