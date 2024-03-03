package com.sapuseven.untis.services

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
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
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject


class CodeScanServiceImpl @Inject constructor(
	private val context: Context
) : CodeScanService, DefaultLifecycleObserver {
	lateinit var registry: ActivityResultRegistry
	lateinit private var scanCodeLauncher: ActivityResultLauncher<ScanOptions>
	private var onSuccess: ((Uri) -> Unit)? = null

	override fun onCreate(owner: LifecycleOwner) {
		scanCodeLauncher = registry.register("scanCode", owner, ScanContract()) { result ->
			result.contents?.let { url ->
				onSuccess?.invoke(Uri.parse(url))
			}
		};
	}

	override fun setResultRegistry(registry: ActivityResultRegistry) {
		this.registry = registry
	}

	override fun scanCode(onSuccess: (Uri) -> Unit) {
		this.onSuccess = onSuccess

		val googleApiAvailability = GoogleApiAvailability.getInstance()
		val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
		/*if (status == ConnectionResult.SUCCESS) scanCodeMlKit()
		else*/ scanCodeFallback()
	}

	private fun scanCodeMlKit() {
		Log.d(CodeScanService::class.java.simpleName, "Using ML Kit")
		val options =
			GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

		GmsBarcodeScanning.getClient(context, options).startScan().addOnSuccessListener { barcode ->
			barcode.rawValue?.let { url -> onSuccess?.invoke(Uri.parse(url)) }
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
