package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.issues.GithubIssue
import com.sapuseven.untis.helpers.issues.Issue
import java.io.File
import java.io.PrintStream

@SuppressLint("Registered") // This activity is not intended to be used directly
open class BaseActivity : AppCompatActivity() {
	private var currentTheme: String = ""
	private var currentDarkTheme: String = ""
	protected lateinit var preferences: PreferenceManager
	protected var hasOwnToolbar: Boolean = false

	private var themeId = -1

	override fun onCreate(savedInstanceState: Bundle?) {
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this, Thread.getDefaultUncaughtExceptionHandler()))

		preferences = PreferenceManager(this)
		currentTheme = PreferenceUtils.getPrefString(preferences, "preference_theme")
		currentDarkTheme = PreferenceUtils.getPrefString(preferences, "preference_dark_theme")
		setAppTheme(hasOwnToolbar)
		super.onCreate(savedInstanceState)
		checkForCrashes()
	}

	private fun checkForCrashes() {
		File(filesDir, "crash").listFiles()?.forEach { crashFile ->
			val reader = crashFile.bufferedReader()

			val stackTrace = StringBuilder()
			val buffer = CharArray(1024)
			var length = reader.read(buffer)

			while (length != -1) {
				stackTrace.append(String(buffer, 0, length))
				length = reader.read(buffer)
			}

			reader.close()
			crashFile.delete()

			MaterialAlertDialogBuilder(this)
					.setTitle(R.string.all_dialog_crash_title)
					.setMessage(stackTrace)
					.setNegativeButton(R.string.all_copy) { _, _ ->
						val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
						clipboard.setPrimaryClip(ClipData.newPlainText("BetterUntis Crash Log", stackTrace))
						Toast.makeText(this, R.string.all_dialog_crash_copied, Toast.LENGTH_SHORT).show()
					}
					.setPositiveButton(R.string.all_close) { dialog, _ ->
						dialog.dismiss()
					}
					.setNeutralButton(R.string.all_report) { _, _ ->
						GithubIssue(Issue.Type.CRASH, stackTrace.toString()).launch(this)
					}
					.show()
		}
	}

	override fun onStart() {
		super.onStart()
		setBlackBackground(PreferenceUtils.getPrefBool(preferences, "preference_dark_theme_oled"))
	}

	override fun onResume() {
		super.onResume()
		val theme = PreferenceUtils.getPrefString(preferences, "preference_theme")
		val darkTheme = PreferenceUtils.getPrefString(preferences, "preference_dark_theme")

		if (currentTheme != theme || currentDarkTheme != darkTheme)
			recreate()

		currentTheme = theme
		currentDarkTheme = darkTheme
	}

	override fun setTheme(resid: Int) {
		super.setTheme(resid)
		themeId = resid
	}

	private fun setAppTheme(hasOwnToolbar: Boolean) {
		when (currentTheme) {
			"untis" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeUntis_NoActionBar else R.style.AppTheme_ThemeUntis)
			"blue" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeBlue_NoActionBar else R.style.AppTheme_ThemeBlue)
			"green" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeGreen_NoActionBar else R.style.AppTheme_ThemeGreen)
			"pink" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemePink_NoActionBar else R.style.AppTheme_ThemePink)
			"cyan" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeCyan_NoActionBar else R.style.AppTheme_ThemeCyan)
			else -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar else R.style.AppTheme)
		}
		delegate.localNightMode = when (PreferenceUtils.getPrefString(preferences, "preference_dark_theme", currentDarkTheme)) {
			"on" -> AppCompatDelegate.MODE_NIGHT_YES
			"auto" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
			else -> AppCompatDelegate.MODE_NIGHT_NO
		}
	}

	private fun setBlackBackground(blackBackground: Boolean) {
		if (blackBackground
				&& resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
			window.decorView.setBackgroundColor(Color.BLACK)
		else {
			val typedValue = TypedValue()
			theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
			if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT)
				window.decorView.setBackgroundColor(typedValue.data)
		}
	}

	protected fun getAttr(@AttrRes attr: Int): Int {
		val typedValue = TypedValue()
		theme.resolveAttribute(attr, typedValue, true)
		return typedValue.data
	}

	class CrashHandler(val context: Context, private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {
		override fun uncaughtException(t: Thread, e: Throwable) {
			Log.e("BetterUntis", "Application crashed!", e)
			saveCrash(e)
			defaultUncaughtExceptionHandler?.uncaughtException(t, e)
		}

		private fun saveCrash(e: Throwable) {
			val parent = File(context.filesDir, "crash")
			parent.mkdir()

			PrintStream(File(parent, "${System.currentTimeMillis()}.log")).use {
				e.printStackTrace(it)
				it.close()
			}
		}
	}
}
