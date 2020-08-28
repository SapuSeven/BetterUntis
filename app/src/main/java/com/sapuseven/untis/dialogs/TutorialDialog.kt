package com.sapuseven.untis.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.sapuseven.untis.R

class TutorialDialog(
		private val context: Context,
		private val prefs: SharedPreferences,
		private val drawer: DrawerLayout,
		menu: Menu
) {
	private val infoCenter = menu.findItem(R.id.nav_infocenter)
	private val freeRooms = menu.findItem(R.id.nav_free_rooms)
	private val inflater = LayoutInflater.from(context)

	private val pages = arrayOf(
			Page(
					title = context.getString(R.string.tutorial_welcome_title),
					message = context.getString(R.string.tutorial_welcome_message)
			),
			Page(
					title = context.getString(R.string.tutorial_timetable_title),
					message = context.getString(R.string.tutorial_timetable_message),
					gravity = Gravity.BOTTOM,
					dim = false
			),
			Page(
					title = context.getString(R.string.tutorial_timetable_item_title),
					message = context.getString(R.string.tutorial_timetable_item_message),
					gravity = Gravity.BOTTOM,
					dim = false
			),
			Page(
					title = context.getString(R.string.tutorial_timetable_picker_title),
					message = context.getString(R.string.tutorial_timetable_picker_message),
					gravity = Gravity.BOTTOM,
					dim = false,
					onShow = {
						drawer.openDrawer(GravityCompat.START)
					}
			),
			Page(
					title = context.getString(R.string.tutorial_info_center_title),
					message = context.getString(R.string.tutorial_info_center_message),
					gravity = Gravity.TOP,
					dim = false,
					onShow = {
						infoCenter.isChecked = true
					},
					onHide = {
						infoCenter.isChecked = false
					}
			),
			Page(
					title = context.getString(R.string.tutorial_free_rooms_title),
					message = context.getString(R.string.tutorial_free_rooms_message),
					gravity = Gravity.TOP,
					dim = false,
					onShow = {
						freeRooms.isChecked = true
					},
					onHide = {
						freeRooms.isChecked = false
						drawer.closeDrawer(GravityCompat.START)
					}
			),
			Page(
					title = context.getString(R.string.tutorial_finish_title),
					message = context.getString(R.string.tutorial_finish_message)
			)
	)

	fun start() {
		showPage(0)
	}

	@SuppressLint("InflateParams")
	private fun showPage(index: Int) {
		val page = pages[index]
		val layout = inflater.inflate(R.layout.dialog_tutorial, null)

		layout.findViewById<TextView>(R.id.progress).text = context.getString(R.string.tutorial_progress_indicator, index + 1, pages.size)
		layout.findViewById<TextView>(R.id.title).text = page.title
		layout.findViewById<TextView>(R.id.message).text = page.message

		page.onShow()

		AlertDialog.Builder(context).apply {
			setCancelable(false)

			if (index > 0) {
				setNegativeButton(context.getString(R.string.tutorial_previous)) { _, _ ->
					showPage(index - 1)
				}
			}
			if (index < pages.size - 1) {
				setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
					page.onHide()
					showPage(index + 1)
				}
			} else {
				setPositiveButton(context.getString(R.string.tutorial_finish)) { _, _ ->
					page.onHide()
				}
			}
			setNeutralButton(context.getString(R.string.tutorial_skip)) { _, _ ->
				page.onHide()
				finishTutorial()
			}
			setView(layout)

			create().apply {
				window?.apply {
					setDimAmount(if (page.dim) 0.32f else 0f)
					attributes.gravity = page.gravity
				}
				show()
			}
		}
	}

	private fun finishTutorial() {
		drawer.closeDrawer(GravityCompat.START)
		prefs.edit().putBoolean("preference_has_finished_tutorial", true).apply()
	}

	class Page(
			val title: String,
			val message: String,
			val onShow: () -> Unit = {},
			val onHide: () -> Unit = {},
			val gravity: Int = Gravity.CENTER,
			val dim: Boolean = true
	)
}
