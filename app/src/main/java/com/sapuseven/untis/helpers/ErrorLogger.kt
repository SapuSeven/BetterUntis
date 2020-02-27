package com.sapuseven.untis.helpers

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintStream

class ErrorLogger(
		context: Context,
		private val filesDir: File? = context.filesDir
) {
	companion object {
		var instance: ErrorLogger? = null

		fun initialize(context: Context) {
			if (instance == null)
				instance = ErrorLogger(context)
		}
	}

	fun log(s: String, details: String? = null) {
		Log.w(ErrorLogger::class.java.simpleName, s)

		val parent = File(filesDir, "logs")
		parent.mkdir()

		PrintStream(generateFile(parent, System.currentTimeMillis().toString())).use { ps ->
			ps.println(s)
			details?.let { ps.println(it) }
			ps.close()
		}
	}

	fun logThrowable(e: Throwable) {
		Log.w(ErrorLogger::class.java.simpleName, e.message ?: "(invalid exception)")

		val parent = File(filesDir, "logs")
		parent.mkdir()

		PrintStream(generateFile(parent, "_" + System.currentTimeMillis().toString())).use {
			e.printStackTrace(it)
			it.close()
		}
	}

	private fun generateFile(parent: File, baseName: String): File {
		File(parent, "${baseName}.log").let {
			return if (!it.exists()) it
			else {
				var i = 0
				var newFile: File

				do {
					i++
					newFile = File(parent, "${baseName}-${i}.log")
				} while (newFile.exists())

				newFile
			}
		}
	}
}
