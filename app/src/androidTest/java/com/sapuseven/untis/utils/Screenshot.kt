package com.sapuseven.untis.utils

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream

private const val TAG_SCREENSHOT = "screenshot"

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
}

@Composable
fun WithScreenshot(content: @Composable ColumnScope.() -> Unit) =
	Column(modifier = Modifier.testTag(TAG_SCREENSHOT), content = content)
