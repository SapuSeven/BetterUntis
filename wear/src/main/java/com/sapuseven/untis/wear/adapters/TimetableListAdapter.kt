package com.sapuseven.untis.wear.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.sapuseven.untis.R

class TimetableListAdapter(private val c: Context, private val parent: ViewGroup) {

    fun clearList() {
        parent.removeAllViews()
    }

    fun resetListLoading() {
        clearList()
        parent.addView(generateView(R.drawable.ic_info, c.resources.getString(R.string.main_loading)))
    }

    fun resetListUnavailable() {
        clearList()
        parent.addView(generateView(R.drawable.ic_info, c.resources.getString(R.string.main_no_timetable_available_for_today)))
    }

    fun addItem(text: String, cancelled: Boolean) {
        parent.addView(generateView(R.drawable.ic_subject, text, cancelled))
    }

    private fun generateView(drawable: Int, title: String, strikeThrough: Boolean = false): TextView {
        val view = (c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.timetable_list_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.main)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
        textView.text = title
        if (strikeThrough) textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        return textView
    }
}