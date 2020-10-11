package com.sapuseven.untis.helpers

import android.content.Context
import android.util.TypedValue

object ConversionUtils {
	fun dpToPx(dp: Float, context: Context): Float {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
	}

	fun spToPx(sp: Float, context: Context): Float {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
	}

	fun dpToSp(dp: Float, context: Context): Int {
		return (dpToPx(dp, context) / context.resources.displayMetrics.scaledDensity).toInt()
	}
}