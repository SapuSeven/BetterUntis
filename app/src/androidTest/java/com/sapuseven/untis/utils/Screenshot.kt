package com.sapuseven.untis.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

const val TAG_SCREENSHOT = "screenshot"
const val SCREENSHOT_PROFILE_NAME = "Example School"

fun ComposeContentTestRule.takeScreenshot(file: String, useRoot: Boolean = false) {
	(if (useRoot) onRoot() else onNodeWithTag(TAG_SCREENSHOT))
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
	Log.d("Screenshot", "Saved screenshot to $basePath/$file")
}

fun loadScreenshot(file: String): Bitmap {
	val basePath = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath + "/screenshots"
	return BitmapFactory.decodeStream(FileInputStream(File(basePath, file)))
}

@Composable
fun WithScreenshot(content: @Composable BoxScope.() -> Unit) = with(LocalDensity.current) {
	Box(
		modifier = Modifier
			.size(1080.toDp(), 1920.toDp())
			.testTag(TAG_SCREENSHOT)
			.consumeWindowInsets(WindowInsets.systemBars),
		content = content
	)
}
