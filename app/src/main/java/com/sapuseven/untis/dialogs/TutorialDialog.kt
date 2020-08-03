package com.sapuseven.untis.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
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
    private val nullParent = null

    fun show(type: Byte = DIALOG_WELCOME, gravity: Int = Gravity.CENTER, dim: Float = 0.32f) {

        val builder = AlertDialog.Builder(context)
        val layout = inflater.inflate(R.layout.dialog_tutorial, nullParent)
        val title = layout.findViewById<TextView>(R.id.title)
        val progress = layout.findViewById<TextView>(R.id.progress)
        val message = layout.findViewById<TextView>(R.id.message)

        when (type) {
            DIALOG_WELCOME -> {
                progress.text = "1/7"
                title.text = context.getString(R.string.tutorial_welcome_title)
                message.text = context.getString(R.string.tutorial_welcome_message)
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE, Gravity.BOTTOM, 0f)
                }
            }
            DIALOG_TIMETABLE -> {
                progress.text = "2/7"
                title.text = context.getString(R.string.tutorial_timetable_title)
                message.text = context.getString(R.string.tutorial_timetable_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_WELCOME)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE_ITEM, Gravity.BOTTOM, 0f)
                }
            }
            DIALOG_TIMETABLE_ITEM -> {
                progress.text = "3/7"
                title.text = context.getString(R.string.tutorial_timetable_item_title)
                message.text = context.getString(R.string.tutorial_timetable_item_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE, Gravity.BOTTOM, 0f)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_TIMETABLE_PICKER, Gravity.BOTTOM, 0f)
                    drawer.openDrawer(GravityCompat.START)
                }
            }
            DIALOG_TIMETABLE_PICKER -> {
                progress.text = "4/7"
                title.text = context.getString(R.string.tutorial_timetable_picker_title)
                message.text = context.getString(R.string.tutorial_timetable_picker_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE_ITEM, Gravity.BOTTOM, 0f)
                    drawer.closeDrawer(GravityCompat.START)
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_INFO_CENTER, Gravity.TOP, 0f)
                }
            }
            DIALOG_INFO_CENTER -> {
                infoCenter.isChecked = true
                progress.text = "5/7"
                title.text = context.getString(R.string.tutorial_info_center_title)
                message.text = context.getString(R.string.tutorial_info_center_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_TIMETABLE_PICKER, Gravity.BOTTOM, 0f)
                    infoCenter.isChecked = false
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_FREE_ROOMS, Gravity.TOP, 0f)
                    infoCenter.isChecked = false
                }
            }
            DIALOG_FREE_ROOMS -> {
                freeRooms.isChecked = true
                progress.text = "6/7"
                title.text = context.getString(R.string.tutorial_free_rooms_title)
                message.text = context.getString(R.string.tutorial_free_rooms_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_INFO_CENTER, Gravity.TOP, 0f)
                    freeRooms.isChecked = false
                }
                builder.setPositiveButton(context.getString(R.string.tutorial_next)) { _, _ ->
                    show(DIALOG_FINISH)
                    drawer.closeDrawer(GravityCompat.START)
                    freeRooms.isChecked = false
                }
            }
            DIALOG_FINISH -> {
                progress.text = "7/7"
                title.text = context.getString(R.string.tutorial_finish_title)
                message.text = context.getString(R.string.tutorial_finish_message)
                builder.setNegativeButton(context.getString(R.string.tutorial_prev)) { _, _ ->
                    show(DIALOG_FREE_ROOMS, Gravity.TOP, 0f)
                    drawer.openDrawer(GravityCompat.START)
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
        builder.setView(layout)

        val dialog = builder.create()
        val window = dialog.window
        window?.setDimAmount(dim)
        window?.attributes?.gravity = gravity
        window?.decorView?.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val toast = Toast.makeText(context, R.string.tutorial_tap_outside, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
            view.performClick()
        }
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