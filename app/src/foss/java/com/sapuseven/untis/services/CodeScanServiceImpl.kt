package com.sapuseven.untis.services

import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.sapuseven.untis.R
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class CodeScanServiceImpl @Inject constructor(
	@ActivityContext val context: Context,
) : CodeScanService {
	private lateinit var scanCodeLauncher: ActivityResultLauncher<ScanOptions>

	override fun setLauncher(launcher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
		this.scanCodeLauncher = launcher
	}

	override fun scanCode(onSuccess: (String?) -> Unit) {
		Log.d(CodeScanService::class.java.simpleName, "Using fallback scanner")
		val options = ScanOptions().apply {
			setDesiredBarcodeFormats(ScanOptions.QR_CODE)
			setBeepEnabled(false)
			setPrompt(context.getString(R.string.login_scan_code))
		}
		scanCodeLauncher.launch(options)
	}
}
