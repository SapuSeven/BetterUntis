package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.models.UntisMessage

class MessageAdapter(
		private val context: Context,
		private val messageList: List<UntisMessage> = ArrayList()
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_twoline, parent, false)
		return ViewHolder(v)
	}

	override fun getItemCount() = messageList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val message = messageList[position]

		holder.tvSubject.text = message.subject
		holder.tvBody.text = HtmlCompat.fromHtml(message.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
		holder.tvBody.movementMethod = LinkMovementMethod.getInstance()

		Log.e(this.javaClass.simpleName, message.attachments.toString())

		holder.tvSubject.visibility = if (message.subject.isBlank()) View.GONE else View.VISIBLE
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvSubject: TextView = rootView.findViewById(R.id.textview_listitem_line1)
		val tvBody: TextView = rootView.findViewById(R.id.textview_listitem_line2)
	}
}
