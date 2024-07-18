package com.sapuseven.untis.ui.navigation

import kotlinx.serialization.Serializable

object AppRoutes {
	@Serializable
	object Splash

	@Serializable
	data class Timetable(val userId: Long) {
		@Serializable
		companion object
	}

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
