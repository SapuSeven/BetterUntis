package com.sapuseven.untis.services

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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

		val googleApiAvailability = GoogleApiAvailability.getInstance()
		val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
		if (status == ConnectionResult.SUCCESS)
			scanCodeMlKit()
		else
			scanCodeFallback()
	}

	private fun scanCodeMlKit() {
		val options = GmsBarcodeScannerOptions.Builder()
			.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
			.build()

		GmsBarcodeScanning.getClient(context, options).startScan()
			.addOnSuccessListener { barcode ->
				barcode.rawValue?.let { url -> onSuccess(Uri.parse(url)) }
			}.addOnFailureListener {
				scanCodeFallback()
			}
	}

	private fun scanCodeFallback() {
		val options = ScanOptions().apply {
			setDesiredBarcodeFormats(ScanOptions.QR_CODE)
			setBeepEnabled(false)
			setPrompt(context.getString(R.string.login_scan_code))
		}
		scanCodeLauncher.launch(options)
	}
}
