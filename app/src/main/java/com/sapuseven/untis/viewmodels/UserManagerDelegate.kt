package com.sapuseven.untis.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UserManagerDelegate {
	val user: StateFlow<User?>
	val allUsers: StateFlow<List<User>?>

	fun init(delegateScope: CoroutineScope, userId: Long)

	fun editUser(user: User?);
	suspend fun deleteUser(user: User);
}

/**
 * This view model provides information about the current user,
 * as well as all other available users.
 */
class UserManagerDelegateImpl @Inject constructor(
	private val userDao: UserDao,
	private val navigator: AppNavigator,
) : ViewModelDelegate(), UserManagerDelegate {
	override fun init(delegateScope: CoroutineScope, userId: Long) {
		init(delegateScope)

		user = flow {
			emit(userDao.getByIdAsync(userId))
		}.stateIn(
			scope = delegateScope,
			started = Eagerly,
			initialValue = null
		)

		allUsers = userDao.getAllFlow().stateIn(
			scope = delegateScope,
			started = WhileSubscribed(2000),
			initialValue = null
		)
	}

	override lateinit var user: StateFlow<User?>

	override lateinit var allUsers: StateFlow<List<User>?>

	override fun editUser(
		user: User?
	) {
		delegateScope.launch {
			navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
		}
	}

	override suspend fun deleteUser(user: User) {
		withContext(Dispatchers.IO) {
			userDao.delete(user)
		}
	}
}
