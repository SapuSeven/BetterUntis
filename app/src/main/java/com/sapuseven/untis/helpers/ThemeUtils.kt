package com.sapuseven.untis.helpers

object ThemeUtils {
	// --Commented out by Inspection START (24-Mar-18 9:16):
	//	@SuppressWarnings("SameParameterValue")
	//	public static void tintDrawable(Context context, Drawable drawable, @AttrRes int attr) {
	//		int[] attrs = new int[]{attr};
	//		TypedArray ta = context.obtainStyledAttributes(attrs);
	//		final int color = ta.getColor(0, 0);
	//		ta.recycle();
	//		DrawableCompat.setTint(drawable, color);
	//	}
	// --Commented out by Inspection STOP (24-Mar-18 9:16)

	/*fun setupTheme(context: Context, actionBar: Boolean) {
		// TODO: Don't use hard-coded strings
		val prefs = PreferenceManager.getDefaultSharedPreferences(context)
		if (actionBar)
			when (prefs.getString("preference_theme", "default")) {
				"untis" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeUntis)
				else
					context.setTheme(R.style.AppTheme_ThemeUntis)
				"blue" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeBlue)
				else
					context.setTheme(R.style.AppTheme_ThemeBlue)
				"green" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeGreen)
				else
					context.setTheme(R.style.AppTheme_ThemeGreen)
				"pink" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemePink)
				else
					context.setTheme(R.style.AppTheme_ThemePink)
				"cyan" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeCyan)
				else
					context.setTheme(R.style.AppTheme_ThemeCyan)
				else -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark)
				else
					context.setTheme(R.style.AppTheme)
			}
		else
			when (prefs.getString("preference_theme", "default")) {
				"untis" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeUntis)
				else
					context.setTheme(R.style.AppTheme_ThemeUntis_NoActionBar)
				"blue" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeBlue)
				else
					context.setTheme(R.style.AppTheme_ThemeBlue_NoActionBar)
				"green" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeGreen)
				else
					context.setTheme(R.style.AppTheme_ThemeGreen_NoActionBar)
				"pink" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemePink)
				else
					context.setTheme(R.style.AppTheme_ThemePink_NoActionBar)
				"cyan" -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark_ThemeCyan)
				else
					context.setTheme(R.style.AppTheme_ThemeCyan_NoActionBar)
				else -> if (prefs.getBoolean("preference_dark_theme", false))
					context.setTheme(R.style.AppThemeDark)
				else
					context.setTheme(R.style.AppTheme_NoActionBar)
			}
	}*/

	/*fun restartApplication(context: Context) {
		val i = Intent(context, MainActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		context.startActivity(i)
	}*/
}