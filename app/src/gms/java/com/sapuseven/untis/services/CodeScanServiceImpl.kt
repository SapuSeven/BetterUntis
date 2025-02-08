package com.sapuseven.untis.services

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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
		val googleApiAvailability = GoogleApiAvailability.getInstance()
		val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
		if (status == ConnectionResult.SUCCESS) scanCodeMlKit(onSuccess)
		else scanCodeFallback()
	}

	private fun scanCodeMlKit(onSuccess: (String?) -> Unit) {
		Log.d(CodeScanService::class.java.simpleName, "Using ML Kit")
		val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

		GmsBarcodeScanning.getClient(context, options).startScan().addOnSuccessListener { barcode ->
			barcode.rawValue?.let { url -> onSuccess(url) }
		}.addOnFailureListener {
			scanCodeFallback()
		}
	}

	private fun scanCodeFallback() {
		Log.d(CodeScanService::class.java.simpleName, "Using fallback scanner")
		val options = ScanOptions().apply {
			setDesiredBarcodeFormats(ScanOptions.QR_CODE)
			setBeepEnabled(false)
			setPrompt(context.getString(R.string.login_scan_code))
		}
		scanCodeLauncher.launch(options)
	}
}
