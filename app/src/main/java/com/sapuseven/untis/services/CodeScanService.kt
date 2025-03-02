package com.sapuseven.untis.services

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

interface CodeScanService {
	fun setLauncher(launcher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>)
	fun scanCode(onSuccess: (String?) -> Unit)
}
