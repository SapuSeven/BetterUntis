package com.sapuseven.untis.modules

import androidx.datastore.preferences.core.edit
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.DataStoreUtil.Companion.USER_ID_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
	@Provides
	fun provideUserManager(
		dataStoreUtil: DataStoreUtil,
		userDao: UserDao
	): UserManager = UserManager(dataStoreUtil, userDao)
}

@Singleton
class UserManager @Inject constructor(
	private val dataStoreUtil: DataStoreUtil,
	private val userDao: UserDao
) {
	private val _userState = MutableStateFlow<User?>(null)
	val activeUser: StateFlow<User?> = _userState

	private val scope = CoroutineScope(Dispatchers.IO)

	init {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.globalDataStore.data.map { preferences ->
				preferences[USER_ID_KEY]
			}.collect { storedActiveUserId ->
				_userState.value = storedActiveUserId?.let { userDao.getById(it) }
			}
		}

	}

	fun setActiveUser(user: User) {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.globalDataStore.edit { preferences ->
				preferences[USER_ID_KEY] = user.id
			}
		}
	}
}
