package com.sapuseven.untis.services

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sapuseven.untis.R

class CodeScanServiceImpl(
	private val context: Context,
	private val registry: ActivityResultRegistry
) : CodeScanService, DefaultLifecycleObserver {
	lateinit var scanCodeLauncher: ActivityResultLauncher<ScanOptions>
	lateinit var onSuccess: (Uri) -> Unit

	override fun onCreate(owner: LifecycleOwner) {
		scanCodeLauncher = registry.register("scanCode", owner, ScanContract()) {
			it.contents?.let { url ->
				if (this::onSuccess.isInitialized) onSuccess(Uri.parse(url))
			}
		}
	}

	override fun scanCode(onSuccess: (Uri) -> Unit) {
		this.onSuccess = onSuccess

		val options = ScanOptions().apply {
			setDesiredBarcodeFormats(ScanOptions.QR_CODE)
			setBeepEnabled(false)
			setPrompt(context.getString(R.string.login_scan_code))
		}
		scanCodeLauncher.launch(options)
	}
}
