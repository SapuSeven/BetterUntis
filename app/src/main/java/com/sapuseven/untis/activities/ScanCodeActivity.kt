package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat
import com.sapuseven.untis.ui.theme.AppTheme

class ScanCodeActivity : BaseComposeActivity() {
	private lateinit var codeScanner: CodeScanner

	companion object {
		const val EXTRA_STRING_SCAN_RESULT = "com.sapuseven.untis.activities.scancode"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val codeScannerView = CodeScannerView(this).apply {
			isAutoFocusButtonVisible = false
		}

		setContent {
			AppTheme {
				AndroidView(factory = { codeScannerView })
			}
		}

		codeScanner = CodeScanner(this, codeScannerView)

		codeScanner.camera = CodeScanner.CAMERA_BACK
		codeScanner.formats = listOf(BarcodeFormat.QR_CODE)
		codeScanner.autoFocusMode = AutoFocusMode.SAFE
		codeScanner.scanMode = ScanMode.SINGLE
		codeScanner.isAutoFocusEnabled = true
		codeScanner.isFlashEnabled = false

		codeScanner.decodeCallback = DecodeCallback {
			val scanResult = Intent()
			scanResult.putExtra(EXTRA_STRING_SCAN_RESULT, it.text)
			setResult(Activity.RESULT_OK, scanResult)
			finish()
		}
		codeScanner.errorCallback = ErrorCallback {
			//setResult()
			finish()
		}

		codeScannerView.setOnClickListener {
			codeScanner.startPreview()
		}
	}

	override fun onResume() {
		super.onResume()
		codeScanner.startPreview()
	}

	override fun onPause() {
		codeScanner.releaseResources()
		super.onPause()
	}
}
