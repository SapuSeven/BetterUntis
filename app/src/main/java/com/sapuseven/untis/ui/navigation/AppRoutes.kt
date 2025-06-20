package com.sapuseven.untis.ui.navigation

import androidx.annotation.StringRes
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import kotlinx.serialization.Serializable

object AppRoutes {
	@Serializable
	data object Splash

	@Serializable
	data class Timetable(
		val type: ElementType? = null,
		val id: Long? = null,
	) {
		constructor(element: PeriodElement?) : this(element?.type, element?.id)

		fun getElement(): PeriodElement? {
			return PeriodElement(
				type ?: return null,
				id ?: return null
			)
		}
	}

	@Serializable
	data object Login

	@Serializable
	data class LoginDataInput(
		val demoLogin: Boolean = false,
		val profileUpdate: Boolean = false,
		val schoolInfoSerialized: String? = null,
		val userId: Long = -1,
		val autoLoginData: String? = null
	)

	@Serializable
	data object InfoCenter {
		@Serializable
		data object Messages

		@Serializable
		data object Events

		@Serializable
		data object Absences

		@Serializable
		data object OfficeHours
	}

	@Serializable
	data object RoomFinder

	@Serializable
	data object Settings {

		@Serializable
		data object Categories

		@Serializable
		data object General

		@Serializable
		data object Styling

		@Serializable
		data class Timetable(@StringRes val highlightTitle: Int = -1)

		@Serializable
		data object Notifications

		@Serializable
		data object About {

			@Serializable
			data object Libraries

			@Serializable
			data object Contributors
		}
	}
}
