package com.sapuseven.untis.services

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry

interface CodeScanService {
	fun setResultRegistry(registry: ActivityResultRegistry);

	fun scanCode(onSuccess: (Uri) -> Unit);
}
