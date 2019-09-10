package com.sapuseven.untis.preferences

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

// Only dialog handling here (frontend)
class ElementPickerPreferenceDialog : PreferenceDialogFragmentCompat() {
	// TODO: This class does nothing
	companion object {
		fun newInstance(key: String): ElementPickerPreferenceDialog {
			val fragment = ElementPickerPreferenceDialog()
			val bundle = Bundle(1)
			bundle.putString(ARG_KEY, key)
			fragment.arguments = bundle
			return fragment
		}
	}

	override fun onDialogClosed(positiveResult: Boolean) {
	}
}
