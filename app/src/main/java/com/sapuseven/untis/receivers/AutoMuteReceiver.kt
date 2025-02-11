package com.sapuseven.untis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceZenRuleImpl
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AutoMuteReceiver @Inject constructor(
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	private val userDao: UserDao,
	private val autoMuteService: AutoMuteService
) : BroadcastReceiver() {
	val settingsRepository = userSettingsRepositoryFactory.create()

	companion object {
		const val EXTRA_BOOLEAN_MUTE = "com.sapuseven.untis.automute.mute"
		const val EXTRA_INT_ID = "com.sapuseven.untis.automute.id"
		const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.automute.userId"
	}

	override fun onReceive(context: Context, intent: Intent) = runBlocking {
		Log.d(
			"AutoMuteReceiver",
			"AutoMuteReceiver received, mute = ${intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)}"
		)

		val userId = intent.getLongExtra(EXTRA_LONG_USER_ID, -1)
		val settings = settingsRepository.getAllSettings().first()
		val userSettings = settings.userSettingsMap.getOrDefault(userId, settingsRepository.getSettingsDefaults())

		if (autoMuteService is AutoMuteServiceZenRuleImpl && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			userDao.getByIdAsync(userId)?.let { autoMuteService.setUser(it) }
		}

		if (intent.hasExtra(EXTRA_BOOLEAN_MUTE)) {
			if (intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)) {
				// Mute
				if (!userSettings.automuteEnable && autoMuteService.isAutoMuteEnabled()) return@runBlocking
				autoMuteService.autoMuteStateOn()
			} else {
				// Unmute
				autoMuteService.autoMuteStateOff()
			}
		}
	}
}
