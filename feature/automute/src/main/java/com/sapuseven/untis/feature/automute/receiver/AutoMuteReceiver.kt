package com.sapuseven.untis.feature.automute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.core.domain.repository.UserRepository
import com.sapuseven.untis.feature.automute.AutoMuteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AutoMuteReceiver : BroadcastReceiver() {
	@Inject
	lateinit var userRepository: UserRepository

	@Inject
	lateinit var autoMuteRepository: AutoMuteRepository

	companion object {
		private val LOG_TAG = AutoMuteReceiver::class.simpleName

		const val EXTRA_INT_ID = "com.sapuseven.untis.automute.id"
		const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.automute.userId"
		const val EXTRA_BOOLEAN_MUTE = "com.sapuseven.untis.automute.mute"
	}

	override fun onReceive(context: Context, intent: Intent) = runBlocking {
		Log.d(LOG_TAG, "AutoMuteReceiver received")

		val user = userRepository.getUserById(intent.getLongExtra(EXTRA_LONG_USER_ID, -1)) ?: return@runBlocking
		autoMuteRepository.autoMuteState(user, intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false))
	}
}
