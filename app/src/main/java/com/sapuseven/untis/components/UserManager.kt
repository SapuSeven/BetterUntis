package com.sapuseven.untis.components

import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.scope.UserScopeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
) {
	private val _userState = MutableStateFlow<UserState>(UserState.Loading)
	val userState: StateFlow<UserState> = _userState

	val allUsersState: StateFlow<List<User>> = userDao.getAllFlow().stateIn(
		scope = CoroutineScope(Dispatchers.IO),
		started = SharingStarted.WhileSubscribed(),
		initialValue = emptyList()
	)

	init {
		loadCurrentUser()
	}

	private fun loadCurrentUser() {
		CoroutineScope(Dispatchers.IO).launch {
			val users = userDao.getAll()
			if (users.isNullOrEmpty()) {
				_userState.value = UserState.NoUsers
			} else {
				switchUser(users.first())
			}
		}
	}

	fun switchUser(user: User) {
		_userState.value = UserState.User(user)
		recreateUserScopedComponents(user)
	}

	fun switchUser(userId: Long) {
		userDao.getById(userId)?.let {
			switchUser(it)
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
