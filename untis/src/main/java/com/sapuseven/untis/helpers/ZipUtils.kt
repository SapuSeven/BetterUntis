package com.sapuseven.untis.helpers

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipUtils {
	companion object {
		private const val BUFFER_SIZE = 1024

		fun zip(srcDir: File, zipFile: File) {
			try {
				val zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
				srcDir.listFiles()?.forEach {
					val buffer = ByteArray(BUFFER_SIZE)
					val fileInputStream = FileInputStream(it)
					zipOutputStream.putNextEntry(ZipEntry(it.name))

					var length: Int = fileInputStream.read(buffer)
					while (length > 0) {
						zipOutputStream.write(buffer, 0, length)
						length = fileInputStream.read(buffer)
					}

					zipOutputStream.closeEntry()
					fileInputStream.close()
				}
				zipOutputStream.close()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}
