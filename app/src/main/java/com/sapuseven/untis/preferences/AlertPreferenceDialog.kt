package com.sapuseven.untis.preferences

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class AlertPreferenceDialog : PreferenceDialogFragmentCompat() {
	companion object {
		fun newInstance(key: String): AlertPreferenceDialog {
			val fragment = AlertPreferenceDialog()
			val bundle = Bundle(1)
			bundle.putString(ARG_KEY, key)
			fragment.arguments = bundle
			return fragment
		}
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		// Nothing to do
	}
}
