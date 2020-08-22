package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sapuseven.untis.R
import com.sapuseven.untis.models.UntisMessage

class MessageAdapter(
		private val context: Context,
		private val messageList: List<UntisMessage> = ArrayList()
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
		return ViewHolder(v)
	}

	override fun getItemCount() = messageList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val message = messageList[position]

		var attachmentTitles: Array<CharSequence> = arrayOf()
		var attachmentURLs: Array<String> = arrayOf()
		message.attachments.forEach {
			attachmentTitles += it.name
			attachmentURLs += it.url
		}

		holder.tvSubject.text = message.subject
		holder.tvBody.text = HtmlCompat.fromHtml(message.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
		holder.tvBody.movementMethod = LinkMovementMethod.getInstance()
		holder.btnAttachments.setOnClickListener {
			MaterialAlertDialogBuilder(context)
					.setTitle(R.string.infocenter_messages_attachments)
					.setItems(attachmentTitles) { _, which ->
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attachmentURLs[which])))
					}
					.show()
		}

		holder.tvSubject.visibility = if (message.subject.isBlank()) View.GONE else View.VISIBLE
		holder.btnAttachments.visibility = if (message.attachments.isEmpty()) View.GONE else View.VISIBLE
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvSubject: TextView = rootView.findViewById(R.id.textview_itemmessage_subject)
		val tvBody: TextView = rootView.findViewById(R.id.textview_itemmessage_body)
		val btnAttachments: Button = rootView.findViewById(R.id.button_itemmessage_attachment)
	}
}
