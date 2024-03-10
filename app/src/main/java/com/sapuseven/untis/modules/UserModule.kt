package com.sapuseven.untis.modules

import androidx.datastore.preferences.core.edit
import com.sapuseven.untis.data.databases.entities.User
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
	fun provideUserId(): Int {
		// TODO: Get the user ID from storage mechanism
		return -1
	}

	@Provides
	fun provideUserManager(dataStoreUtil: DataStoreUtil): UserManager = UserManager(dataStoreUtil)
}

@Singleton
class UserManager @Inject constructor(
	private val dataStoreUtil: DataStoreUtil
) {
	private val _userState = MutableStateFlow<Long?>(null)
	val activeUser: StateFlow<Long?> = _userState

	private val scope = CoroutineScope(Dispatchers.IO)

	init {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.globalDataStore.data.map { preferences ->
				preferences[USER_ID_KEY]
			}.collect {
				_userState.value = it
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
