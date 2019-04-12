package com.sapuseven.untis.preferences

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceDialogFragmentCompat
import com.sapuseven.untis.R


class AlertPreferenceDialog : PreferenceDialogFragmentCompat() {
	companion object {
		fun newInstance(key: String): AlertPreferenceDialog {
			val fragment = AlertPreferenceDialog()
			val bundle = Bundle(1)
			bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
			fragment.arguments = bundle
			return fragment
		}
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		// Nothing to do
	}
}
