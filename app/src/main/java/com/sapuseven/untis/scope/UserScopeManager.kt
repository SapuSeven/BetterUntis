package com.sapuseven.untis.scope

import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.modules.UserComponentEntryPoint
import dagger.hilt.EntryPoints
import javax.inject.Inject
import javax.inject.Singleton

interface UserScopeManager {
	val user: User
	val userOptional: User?

	fun handleUserChange(user: User);
}

@Singleton
class UserScopeManagerImpl @Inject constructor(
	private var userComponentBuilder: UserComponent.Builder
): UserScopeManager {
	private var component: UserComponent? = null
	private var entryPoint: UserComponentEntryPoint? = null

	override val user: User
		get() = userOptional!!

	override val userOptional: User?
		get() = entryPoint?.getUser()

	/**
	 * Get or create the UserComponent for the given user.
	 */
	private fun getUserComponent(user: User) {
		userComponentBuilder
			.user(user)
			.build()
			.also {
				component = it
				entryPoint = EntryPoints.get(it, UserComponentEntryPoint::class.java)
			}
	}

	/**
	 * Clear the current user component.
	 */
	private fun clearUserComponent() {
		component = null
		entryPoint = null
	}

	/**
	 * Handle user change by recreating the user-scoped component.
	 */
	override fun handleUserChange(user: User) {
		clearUserComponent()
		getUserComponent(user)
	}
}
