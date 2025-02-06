package com.sapuseven.untis.modules

import android.app.NotificationManager
import android.content.Context
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceZenRuleImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AutoMuteModule {
	@Provides
	fun provideAutoMuteService(
		@ApplicationContext context: Context,
		userScopeManager: UserScopeManager
	): AutoMuteService {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
			return AutoMuteServiceZenRuleImpl(
				context,
				context.getSystemService(NotificationManager::class.java) as NotificationManager,
				userScopeManager
			)
		} else {
			throw NotImplementedError("AutoMuteServiceDirectImpl is not implemented")
		}
	}
}
