package com.sapuseven.untis.modules

import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.domain.GetMessagesOfDayUseCase
import com.sapuseven.untis.domain.GetRoomFinderItemsUseCase
import com.sapuseven.untis.models.RoomFinderItem
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton


@Module
@TestInstallIn(
	components = [SingletonComponent::class],
	replaces = [UseCaseModule::class]
)
class ScreenshotUseCaseModule {
	@Singleton
	@Provides
	fun provideGetMessagesUseCase(): GetMessagesOfDayUseCase = object : GetMessagesOfDayUseCase {
		override operator fun invoke(): Flow<Result<List<MessageOfDay>>> = flow {
			emit(
				Result.success(
					listOf(
						MessageOfDay(
							1,
							"School messages...",
							"You can view messages from your school here in the <b>Info Center</b>.",
							emptyList()
						),
						MessageOfDay(
							2,
							"...and more!",
							"There are even more tabs with other useful information at the bottom:<br>" +
								"<br><ul>" +
								"  <li>&nbsp;<b>Events</b> shows all upcoming exams and homework assignments.</li>" +
								"  <li>&nbsp;<b>Absences</b> lets you manage your absences.</li>" +
								"  <li>&nbsp;<b>Office Hours</b> lists the available office hours for teachers.</li>" +
								"</ul>",
							emptyList()
						)
					)
				)
			)
		}
	}

	@Singleton
	@Provides
	fun provideGetRoomFinderItemsUseCase(): GetRoomFinderItemsUseCase = object : GetRoomFinderItemsUseCase {
		override operator fun invoke(): Flow<List<RoomFinderItem>> = flow {
			emit(
				listOf(
					RoomFinderItem(RoomEntity(id = 1, longName = "A001"), listOf(true, false, false, false)),
					RoomFinderItem(RoomEntity(id = 2, longName = "A002"), listOf(true, true, false, false)),
					RoomFinderItem(RoomEntity(id = 3, longName = "A003"), listOf(true, false, false, true)),
					RoomFinderItem(RoomEntity(id = 4, longName = "A004"), listOf(false, false, false, true)),
					RoomFinderItem(RoomEntity(id = 5, longName = "A005"), listOf(false, false, true, true))
				)
			)
		}
	}
}
