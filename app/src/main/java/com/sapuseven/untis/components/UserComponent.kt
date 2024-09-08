package com.sapuseven.untis.components

import com.sapuseven.untis.data.databases.entities.User
import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent

@DefineComponent(parent = SingletonComponent::class)
interface UserComponent {
	@DefineComponent.Builder
	interface Builder {
		@BindsInstance
		fun user(user: User): Builder

		fun build(): UserComponent
	}
}
