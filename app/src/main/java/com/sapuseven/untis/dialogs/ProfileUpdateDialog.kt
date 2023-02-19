package com.sapuseven.untis.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.entities.User


class ProfileUpdateDialog : Fragment() {
	private var user: User? = null

	companion object {
		fun createInstance(user: User): ProfileUpdateDialog {
			return ProfileUpdateDialog().apply {
				this.user = user
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.activity_logindatainput_profile_update, container, false)
	}
}
