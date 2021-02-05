package com.sapuseven.untis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase

class ProfileListAdapter(
		private val context: Context,
		private val dataset: MutableList<UserDatabase.User>,
		private val onClickListener: View.OnClickListener,
		private val onLongClickListener: View.OnLongClickListener
) :
		RecyclerView.Adapter<ProfileListAdapter.ViewHolder>() {

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		var tvName: TextView = itemView.findViewById(R.id.textview_profiles_name)
		var tvSchool: TextView = itemView.findViewById(R.id.textview_profiles_school)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profiles, parent, false) as ConstraintLayout
		v.setOnClickListener(onClickListener)
		v.setOnLongClickListener(onLongClickListener)
		return ViewHolder(v)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = dataset[position]
		holder.tvName.text = item.getDisplayedName(context)
		holder.tvSchool.text = item.userData.schoolName
	}

	override fun getItemCount() = dataset.size

	fun deleteUser(user: UserDatabase.User) = dataset.remove(user)

	fun itemAt(position: Int) = dataset[position]
}
