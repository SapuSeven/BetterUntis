package com.sapuseven.untis.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoMuteSetupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
	override fun doWork(): Result {
		return Result.success()
	}
}
