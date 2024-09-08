package com.sapuseven.untis.components

import androidx.datastore.core.DataStore
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	private val userSettings: DataStore<UserSettings>,
) {
	private val _userState = MutableStateFlow<UserState>(UserState.Loading)
	val userState: StateFlow<UserState> = _userState

	val allUsersState: StateFlow<List<User>> = userDao.getAllFlow().stateIn(
		scope = CoroutineScope(Dispatchers.IO),
		started = SharingStarted.WhileSubscribed(),
		initialValue = emptyList()
	)

	init {
		loadActiveUser()
	}

	private fun loadActiveUser() {
		CoroutineScope(Dispatchers.IO).launch {
			switchUser(userSettings.data.first().activeUser)
		}
	}

	fun switchUser(user: User) {
		_userState.value = UserState.User(user)
		recreateUserScopedComponents(user)
		CoroutineScope(Dispatchers.IO).launch {
			userSettings.updateData { currentSettings ->
				currentSettings.toBuilder()
					.setActiveUser(user.id)
					.build()
			}
		}
	}

	suspend fun switchUser(userId: Long) {
		val user = userDao.getByIdAsync(userId)
			?: userDao.getAllFlow().first().firstOrNull()

		user?.let {
			switchUser(it)
		} ?: run {
			_userState.value = UserState.NoUsers
		}
	}

	fun deleteUser(user: User) {
		CoroutineScope(Dispatchers.IO).launch {
			userDao.delete(user)

			val remainingUsers = userDao.getAll()
			val currentState = _userState.value
			if (currentState is UserState.User && currentState.user == user) {
				if (remainingUsers.isNullOrEmpty()) {
					_userState.value = UserState.NoUsers
				} else {
					_userState.value = UserState.User(remainingUsers.first())
				}
			}
		}
	}

	private fun recreateUserScopedComponents(user: User) {
		userScopeManager.handleUserChange(user)
	}
}

sealed class UserState {
	data object Loading : UserState()
	data object NoUsers : UserState()
	data class User(val user: com.sapuseven.untis.data.databases.entities.User) : UserState()
}
