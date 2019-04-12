package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils

@SuppressLint("Registered") // This activity is not intended to be used directly
open class BaseActivity : AppCompatActivity() {
	private var currentTheme: String = ""
	private var currentDarkTheme: String = ""
	private lateinit var preferenceManager: PreferenceManager
	protected var hasOwnToolbar: Boolean = false

	private var themeId = -1

	override fun onCreate(savedInstanceState: Bundle?) {
		preferenceManager = PreferenceManager(this)
		currentTheme = PreferenceUtils.getPrefString(preferenceManager, "preference_theme")
		currentDarkTheme = PreferenceUtils.getPrefString(preferenceManager, "preference_dark_theme")
		setAppTheme(hasOwnToolbar)
		setBlackBackground(PreferenceUtils.getPrefBool(preferenceManager, "preference_dark_theme_oled"))
		super.onCreate(savedInstanceState)
	}

	override fun onResume() {
		super.onResume()
		val theme = PreferenceUtils.getPrefString(preferenceManager, "preference_theme")
		val darkTheme = PreferenceUtils.getPrefString(preferenceManager, "preference_dark_theme")

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
			"untis" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar_ThemeUntis else R.style.AppTheme_ThemeUntis)
			"blue" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar_ThemeBlue else R.style.AppTheme_ThemeBlue)
			"green" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar_ThemeGreen else R.style.AppTheme_ThemeGreen)
			"pink" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar_ThemePink else R.style.AppTheme_ThemePink)
			"cyan" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar_ThemeCyan else R.style.AppTheme_ThemeCyan)
			else -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar else R.style.AppTheme)
		}
		delegate.setLocalNightMode(when (PreferenceUtils.getPrefString(preferenceManager, "preference_dark_theme", currentDarkTheme)) {
			"on" -> AppCompatDelegate.MODE_NIGHT_YES
			"auto" -> AppCompatDelegate.MODE_NIGHT_AUTO
			else -> AppCompatDelegate.MODE_NIGHT_NO
		})
	}

	private fun setBlackBackground(blackBackground: Boolean) {
		if (blackBackground)
			window.decorView.setBackgroundColor(Color.BLACK)
	}

	protected fun getAttr(@AttrRes attr: Int): Int {
		val typedValue = TypedValue()
		theme.resolveAttribute(attr, typedValue, true)
		return typedValue.data
	}
}
