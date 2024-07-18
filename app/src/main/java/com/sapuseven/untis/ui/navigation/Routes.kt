package com.sapuseven.untis.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.Serializable

object Routes {
	@Serializable
	object Splash

	@Serializable
	data class Timetable(val userId: Long)

	@Serializable
	object Login

	@Serializable
	data class LoginDataInput(
		val demoLogin: Boolean = false,
		val profileUpdate: Boolean = false,
		val schoolInfoSerialized: String? = null,
		val userId: Long = -1
	)

	@Serializable
	data class Settings(val userId: Long)
}
