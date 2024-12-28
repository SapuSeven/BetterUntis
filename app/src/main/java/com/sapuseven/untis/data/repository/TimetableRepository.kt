package com.sapuseven.untis.data.repository

import android.content.Context
import androidx.compose.material3.ColorScheme
import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.cache.DiskCache
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.time.SystemTimeProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.serializer
import java.io.File
import java.time.LocalDate

class TimetableRepository @AssistedInject constructor(
	private val api: TimetableApi,
	private val userScopeManager: UserScopeManager,
	private val timetableMapperFactory: TimetableMapper.Factory,
	@Assisted private val colorScheme: ColorScheme,
	@ApplicationContext private val appContext: Context
) {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): TimetableRepository
	}

	private val user: User = userScopeManager.user
	private val timetableMapper = timetableMapperFactory.create(colorScheme)

	suspend fun timetableSource(): CachedSource<TimetableParams, List<Period>> {
		return CachedSource(
			source = { params ->
				api.loadTimetable(
					id = params.elementId,
					type = params.elementType,
					startDate = params.startDate,
					endDate = params.endDate,
					masterDataTimestamp = user.masterDataTimestamp,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).timetable.periods
			},
			cache = DiskCache(File(appContext.cacheDir, "timetable"), serializer()),
			timeProvider = SystemTimeProvider // TODO: Use from DI to allow for testing
		)
	}

	data class TimetableParams(
		val elementId: Long,
		val elementType: ElementType,
		val startDate: LocalDate,
		val endDate: LocalDate = startDate.plusDays(5 /*TODO*/)
	)
}
