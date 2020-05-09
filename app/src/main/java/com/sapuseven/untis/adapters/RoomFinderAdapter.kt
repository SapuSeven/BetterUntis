package com.sapuseven.untis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R

class RoomFinderAdapter(
		private val context: Context,
		private val onClickListener: RoomFinderClickListener,
		private val roomList: List<RoomFinderAdapterItem> = ArrayList()
) : RecyclerView.Adapter<RoomFinderAdapter.ViewHolder>() {

	var currentHourIndex: Int = 0
		set(value) {
			field = value
			roomList.forEach { it.hourIndex = value }
		}

	init {
		setHasStableIds(true)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_roomfinder, parent, false)
		v.setOnClickListener(onClickListener)
		return ViewHolder(v)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val room = roomList[position]

		holder.tvName.text = room.name

		when {
			room.getState() == RoomFinderAdapterItem.STATE_OCCUPIED -> holder.tvDetails.text = context.resources.getString(R.string.roomfinder_item_desc_occupied)
			room.getState() >= RoomFinderAdapterItem.STATE_FREE -> holder.tvDetails.text = context.resources.getQuantityString(R.plurals.roomfinder_item_desc, room.getState(), room.getState())
			else -> holder.tvDetails.text = context.resources.getString(R.string.roomfinder_loading_data)
		}

		if (room.getState() >= RoomFinderAdapterItem.STATE_FREE && !room.loading) {
			holder.ivState.setImageResource(R.drawable.all_check)
			holder.ivState.visibility = View.VISIBLE
			holder.pbLoading.visibility = View.GONE
		} else if (room.getState() == RoomFinderAdapterItem.STATE_OCCUPIED && !room.loading) {
			holder.ivState.setImageResource(R.drawable.all_cross)
			holder.ivState.visibility = View.VISIBLE
			holder.pbLoading.visibility = View.GONE
		} else {
			holder.ivState.visibility = View.GONE
			holder.pbLoading.visibility = View.VISIBLE
		}

		holder.btnDelete.setOnClickListener { onClickListener.onDeleteClick(holder.adapterPosition) }
	}

	override fun getItemCount(): Int {
		return roomList.size
	}

	override fun getItemId(position: Int): Long {
		return roomList[position].hashCode().toLong()
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvName: TextView = rootView.findViewById(R.id.textview_roomfinder_name)
		val tvDetails: TextView = rootView.findViewById(R.id.textview_roomfinder_details)
		val ivState: AppCompatImageView = rootView.findViewById(R.id.imageview_roomfinder_state)
		val pbLoading: ProgressBar = rootView.findViewById(R.id.progressbar_roomfinder_loading)
		val btnDelete: ImageButton = rootView.findViewById(R.id.button_roomfinder_delete)
	}

	interface RoomFinderClickListener : View.OnClickListener {
		fun onDeleteClick(position: Int)
	}
}
