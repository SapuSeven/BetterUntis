package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity

@SuppressLint("Registered") // This activity is not intended to be used directly
open class BaseComposeActivity : ComponentActivity() {
	/*protected var hasOwnToolbar: Boolean = false
	protected var currentTheme: String = ""
	private var currentDarkTheme: String = ""

	protected lateinit var preferences: PreferenceHelper*/

	override fun onCreate(savedInstanceState: Bundle?) {
		/*ErrorLogger.initialize(this)

		Thread.setDefaultUncaughtExceptionHandler(CrashHandler(Thread.getDefaultUncaughtExceptionHandler()))

		preferences = PreferenceHelper(this)
		preferences.loadSavedProfile()

		currentTheme = preferences["preference_theme"]
		currentDarkTheme = preferences["preference_dark_theme"]
		setAppTheme(hasOwnToolbar)*/
		super.onCreate(savedInstanceState)
	}
/*
	/**
	 * Checks for saved crashes. Calls [onErrorLogFound] if logs are found.
	 *
	 * @return `true` if the logs contain a critical application crash, `false` otherwise
	 */
	protected fun checkForCrashes(): Boolean {
		val logFiles = File(filesDir, "logs").listFiles()
		if (logFiles?.isNotEmpty() == true) {
			onErrorLogFound()

			return logFiles.find { f -> f.name.startsWith("_") } != null
		}
		return false
	}

	/**
	 * Gets called if any error logs are found.
	 *
	 * Override this function in your actual activity.
	 */
	open fun onErrorLogFound() {
		return
	}

	protected fun readCrashData(crashFile: File): String {
		val reader = crashFile.bufferedReader()

		val stackTrace = StringBuilder()
		val buffer = CharArray(1024)
		var length = reader.read(buffer)

		while (length != -1) {
			stackTrace.append(String(buffer, 0, length))
			length = reader.read(buffer)
		}

		return stackTrace.toString()
	}

	override fun onStart() {
		super.onStart()
		setBlackBackground(preferences["preference_dark_theme_oled"])
	}

	override fun onResume() {
		super.onResume()
		val theme: String = preferences["preference_theme"]
		val darkTheme: String = preferences["preference_dark_theme"]

		if (currentTheme != theme || currentDarkTheme != darkTheme)
			recreate()

		currentTheme = theme
		currentDarkTheme = darkTheme
	}

	private fun setAppTheme(hasOwnToolbar: Boolean) {
		when (currentTheme) {
			"untis" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeUntis_NoActionBar else R.style.AppTheme_ThemeUntis)
			"blue" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeBlue_NoActionBar else R.style.AppTheme_ThemeBlue)
			"green" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeGreen_NoActionBar else R.style.AppTheme_ThemeGreen)
			"pink" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemePink_NoActionBar else R.style.AppTheme_ThemePink)
			"cyan" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeCyan_NoActionBar else R.style.AppTheme_ThemeCyan)
			"pixel" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemePixel_NoActionBar else R.style.AppTheme_ThemePixel)
			else -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar else R.style.AppTheme)
		}

		AppCompatDelegate.setDefaultNightMode(
			when (preferences["preference_dark_theme", currentDarkTheme]) {
				"on" -> AppCompatDelegate.MODE_NIGHT_YES
				"off" -> AppCompatDelegate.MODE_NIGHT_NO
				else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
			}
		)
	}

	private fun setBlackBackground(blackBackground: Boolean) {
		if (blackBackground
			&& resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
		)
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

	private class CrashHandler(private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?) :
		Thread.UncaughtExceptionHandler {
		override fun uncaughtException(t: Thread, e: Throwable) {
			Log.e("BetterUntis", "Application crashed!", e)
			saveCrash(e)
			defaultUncaughtExceptionHandler?.uncaughtException(t, e)
		}

		private fun saveCrash(e: Throwable) {
			ErrorLogger.instance?.logThrowable(e)
		}
	}*/
}
