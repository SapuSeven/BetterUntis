package com.sapuseven.untis.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import android.view.Menu
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

    fun show(type: Byte = DIALOG_WELCOME, gravity: Int = Gravity.CENTER, dim: Float = 0.32f) {

        val builder = AlertDialog.Builder(context)
        when (type) {
            DIALOG_WELCOME -> {
                builder.setTitle(context.getString(R.string.tutorial_welcome_title))
                builder.setMessage(context.getString(R.string.tutorial_welcome_message))
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE, Gravity.BOTTOM, 0f)
                }
            }
            DIALOG_TIMETABLE -> {
                builder.setTitle(context.getString(R.string.tutorial_timetable_title))
                builder.setMessage(context.getString(R.string.tutorial_timetable_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_WELCOME)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE_ITEM, Gravity.BOTTOM, 0f)
                }
            }
            DIALOG_TIMETABLE_ITEM -> {
                builder.setTitle(context.getString(R.string.tutorial_timetable_item_title))
                builder.setMessage(context.getString(R.string.tutorial_timetable_item_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE, Gravity.BOTTOM, 0f)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE_PICKER, Gravity.BOTTOM, 0f)
                    drawer.openDrawer(GravityCompat.START)
                }
            }
            DIALOG_TIMETABLE_PICKER -> {
                builder.setTitle(context.getString(R.string.tutorial_timetable_picker_title))
                builder.setMessage(context.getString(R.string.tutorial_timetable_picker_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE_ITEM, Gravity.BOTTOM, 0f)
                    drawer.closeDrawer(GravityCompat.START)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_INFO_CENTER, Gravity.TOP, 0f)
                    infoCenter.isChecked = true
                }
            }
            DIALOG_INFO_CENTER -> {
                builder.setTitle(context.getString(R.string.tutorial_info_center_title))
                builder.setMessage(context.getString(R.string.tutorial_info_center_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE_PICKER, Gravity.BOTTOM, 0f)
                    infoCenter.isChecked = false
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_FREE_ROOMS, Gravity.TOP, 0f)
                    infoCenter.isChecked = false
                    freeRooms.isChecked = true
                }
            }
            DIALOG_FREE_ROOMS -> {
                builder.setTitle(context.getString(R.string.tutorial_free_rooms_title))
                builder.setMessage(context.getString(R.string.tutorial_free_rooms_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_INFO_CENTER, Gravity.TOP, 0f)
                    freeRooms.isChecked = false
                    infoCenter.isChecked = true
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_FINISH)
                    drawer.closeDrawer(GravityCompat.START)
                    freeRooms.isChecked = false
                }
            }
            DIALOG_FINISH -> {
                builder.setTitle(context.getString(R.string.tutorial_finish_title))
                builder.setMessage(context.getString(R.string.tutorial_finish_message))
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_FREE_ROOMS, Gravity.TOP, 0f)
                    drawer.openDrawer(GravityCompat.START)
                    freeRooms.isChecked = true
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_finish)) { _, _ ->
                    finishTutorial()
                }
            }
        }

        builder.setCancelable(false)
        builder.setNeutralButton(context.getString(R.string.tutorial_skip)){ _, _ ->
            finishTutorial()
        }

        val dialog = builder.create()
        val window = dialog.window
        window?.setDimAmount(dim)
        window?.attributes?.gravity = gravity
        dialog.show()
    }

    private fun finishTutorial() {
        infoCenter.isChecked = false
        freeRooms.isChecked = false
        prefs.edit().putBoolean("preference_has_finished_tutorial", true).apply()
    }

    companion object {
        private const val DIALOG_WELCOME: Byte = 0
        private const val DIALOG_TIMETABLE: Byte = 1
        private const val DIALOG_TIMETABLE_ITEM: Byte = 2
        private const val DIALOG_TIMETABLE_PICKER: Byte = 3
        private const val DIALOG_INFO_CENTER: Byte = 4
        private const val DIALOG_FREE_ROOMS: Byte = 5
        private const val DIALOG_FINISH: Byte = 6
    }
}