package com.sapuseven.untis.views

import android.content.Context
import android.util.AttributeSet
import com.sapuseven.untis.views.weekview.WeekView

class WeekViewSwipeRefreshLayout : androidx.swiperefreshlayout.widget.SwipeRefreshLayout {
	constructor(context: Context) : super(context, null)
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	override fun canChildScrollUp(): Boolean {
		for (i in 0..childCount) {
			val child = getChildAt(i)

			if (child is WeekView<*>)
				return !child.swipeRefreshAvailable()
		}

		return true
	}
}