package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.RoomFinderDao
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class GetRoomFinderItemsUseCase @Inject constructor(
	private val roomFinderDao: RoomFinderDao,
	private val timetableRepository: TimetableRepository,
	masterDataRepository: MasterDataRepository,
	userScopeManager: UserScopeManager
) {
	private val user: User = userScopeManager.user
	private val rooms = masterDataRepository.currentUserData?.rooms ?: emptyList()

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	private val roomData: MutableMap<RoomEntity, List<Boolean>> = mutableMapOf()

	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<List<RoomFinderItem>> = roomFinderDao.getAllByUserId(user.id)
		.mapNotNull { roomFinderEntities ->
			roomFinderEntities?.mapNotNull { roomFinderEntity ->
				rooms.find { it.id == roomFinderEntity.id }
			}
		}
		.flatMapLatest { roomEntities ->
			flow {
				emitRoomData(roomEntities)

				roomEntities.forEach { roomEntity ->
					fetchRoomStates(roomEntity).collect {
						roomData[roomEntity] = it
						emitRoomData(roomEntities)
					}
				}
			}
		}

	private suspend fun FlowCollector<List<RoomFinderItem>>.emitRoomData(roomEntities: List<RoomEntity>) {
		emit(roomEntities.map { roomEntity ->
			RoomFinderItem(roomEntity, roomData.getOrDefault(roomEntity, emptyList()))
		})
	}

	private fun fetchRoomStates(room: RoomEntity): Flow<List<Boolean>> {
		val startDate = LocalDate.now().with(DayOfWeek.MONDAY) // TODO

		return timetableRepository.timetableSource().get(
			params = TimetableRepository.TimetableParams(
				room.id,
				ElementType.ROOM,
				startDate
			),
			fromCache = FromCache.IF_HAVE, // Use a conservative caching strategy to reduce API request count
			maxAge = ONE_HOUR,
			additionalKey = user.id
		).map(::mapPeriodsToBooleanList)
	}

	private fun mapPeriodsToBooleanList(periods: List<Period>): List<Boolean> {
		return user.timeGrid.days.flatMap { day ->
			day.units.map { unit ->
				periods.any { period ->
					!period.`is`(PeriodState.CANCELLED) &&
						period.startDateTime.dayOfWeek == day.day &&
						unit.endTime.isAfter(period.startDateTime.toLocalTime()) &&
						unit.startTime.isBefore(period.endDateTime.toLocalTime())
				}
			}
		}
	}
}
