package com.sapuseven.untis.ui.viewmodels

import androidx.lifecycle.LiveData
import com.sapuseven.untis.data.databases.entities.User

/**
 * This view model provides information about the current user,
 * as well as all other available users.
 *
 * TODO: Make this an actual class and move user related code here
 */
interface UserViewModel {
	fun editUser(user: User?);

	suspend fun deleteUser(user: User);

	public fun getAllUsers(): LiveData<List<User>>;
}
