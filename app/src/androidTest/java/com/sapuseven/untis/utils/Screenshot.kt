package com.sapuseven.untis.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream

fun ComposeContentTestRule.takeScreenshot(file: String) {
	onRoot()
		.captureToImage()
		.asAndroidBitmap()
		.save(file)
}

fun Bitmap.save(file: String) {
	val basePath = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath + "/screenshots"
	File(basePath).mkdirs()
	FileOutputStream(File(basePath, file)).use { out ->
		compress(Bitmap.CompressFormat.PNG, 100, out)
	}
}
