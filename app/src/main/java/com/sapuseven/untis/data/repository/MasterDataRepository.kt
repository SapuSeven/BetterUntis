package com.sapuseven.untis.data.repository

import androidx.compose.runtime.compositionLocalOf
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.persistence.entity.ElementEntity
import com.sapuseven.untis.persistence.entity.User
import com.sapuseven.untis.persistence.entity.UserDao
import com.sapuseven.untis.persistence.entity.UserWithData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface MasterDataRepository {
	val user: User?
	val userData: UserWithData?

	val classes: Flow<List<ElementEntity>>
	val teachers: Flow<List<ElementEntity>>
	val subjects: Flow<List<ElementEntity>>
	val rooms: Flow<List<ElementEntity>>

	fun getElement(id: Long, type: ElementType): ElementEntity?
}

@Singleton
class DefaultMasterDataRepository : MasterDataRepository {
	override val user: User? = null
	override val userData: UserWithData? = null

	override val classes: Flow<List<ElementEntity>> = flowOf(emptyList())
	override val teachers: Flow<List<ElementEntity>> = flowOf(emptyList())
	override val subjects: Flow<List<ElementEntity>> = flowOf(emptyList())
	override val rooms: Flow<List<ElementEntity>> = flowOf(emptyList())

	override fun getElement(id: Long, type: ElementType): ElementEntity? = null
}

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UntisMasterDataRepository @Inject constructor(
	private val userDao: UserDao,
	private val userRepository: UserRepository
) : MasterDataRepository {
	override val user: User?
		get() = (userRepository.userState.value as? UserRepository.UserState.User)?.user

	private val _userData = MutableStateFlow<UserWithData?>(null)
	override val userData: UserWithData?
		get() = _userData.value

	// TODO Delete userData and UserWithData and add flows for all required attributes

	private val userIdFlow = userRepository.userState
		.map { (it as? UserRepository.UserState.User)?.user?.id }
		.distinctUntilChanged()

	override val classes = userIdFlow
		.flatMapLatest { id ->
			id?.let { userDao.getActiveClassesFlow(it) } ?: flowOf(emptyList())
		}

	override val teachers = userIdFlow
		.flatMapLatest { id ->
			id?.let { userDao.getActiveTeachersFlow(it) } ?: flowOf(emptyList())
		}

	override val subjects = userIdFlow
		.flatMapLatest { id ->
			id?.let { userDao.getActiveSubjectsFlow(it) } ?: flowOf(emptyList())
		}

	override val rooms = userIdFlow
		.flatMapLatest { id ->
			id?.let { userDao.getActiveRoomsFlow(it) } ?: flowOf(emptyList())
		}

	override fun getElement(id: Long, type: ElementType): ElementEntity? {
		// TODO well, now this can only be called from a location where we have this state.
		//  This needs to be redesigned
		return when (type) {
			//ElementType.CLASS -> classes.value.firstOrNull { it.id == id }
			//ElementType.TEACHER -> teachers.value.firstOrNull { it.id == id }
			//ElementType.SUBJECT -> subjects.value.firstOrNull { it.id == id }
			//ElementType.ROOM -> rooms.value.firstOrNull { it.id == id }
			else -> null
		}
	}
}

val LocalMasterDataRepository = compositionLocalOf<MasterDataRepository> { DefaultMasterDataRepository() }
