package com.sapuseven.untis.feature.automute

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.sapuseven.untis.core.domain.service.AutoMuteService
import com.sapuseven.untis.core.domain.worker.TimetableHandler
import com.sapuseven.untis.feature.automute.service.AutoMuteServiceInterruptionFilterImpl
import com.sapuseven.untis.feature.automute.service.AutoMuteServiceRingerModeImpl
import com.sapuseven.untis.feature.automute.service.AutoMuteServiceZenRuleImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AutoMuteModule {
	@Binds
	@IntoSet
	abstract fun bindAutoMuteScheduler(impl: AutoMuteScheduler): TimetableHandler

	companion object {
		@Provides
		fun provideNotificationManager(
			@ApplicationContext context: Context
		): NotificationManager? = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

		@Provides
		fun provideAutoMuteService(
			@ApplicationContext context: Context,
			notificationManager: NotificationManager?
		): AutoMuteService = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			AutoMuteServiceZenRuleImpl(context, notificationManager!!)
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			AutoMuteServiceInterruptionFilterImpl(notificationManager!!)
		} else {
			AutoMuteServiceRingerModeImpl(context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
		}
	}
}
