package com.sapuseven.untis.modules

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceStub
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
		@ApplicationContext context: Context
	): AutoMuteService {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			AutoMuteServiceZenRuleImpl(
				context,
				context.getSystemService(NotificationManager::class.java) as NotificationManager
			)
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			throw NotImplementedError("AutoMuteServiceDirectImpl is not implemented")
		} else {
			AutoMuteServiceStub()
		}
	}
}
