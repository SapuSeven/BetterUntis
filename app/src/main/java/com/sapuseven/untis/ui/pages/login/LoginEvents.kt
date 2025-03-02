package com.sapuseven.untis.ui.pages.login

sealed class LoginEvents {
	data object ClearFocus : LoginEvents()
}
