package com.sapuseven.untis.activities

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.AlertPreference
import com.sapuseven.untis.preferences.AlertPreferenceDialog
import com.sapuseven.untis.preferences.ElementPickerPreference
import kotlinx.android.synthetic.main.activity_settings.*

// TODO: The actionbar back arrow still exits the entire activity; go back on the backstack instead
// TODO: The current settings page should be displayed in the actionbar
class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
	private var profileId: Long? = null

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileId"

		private const val DIALOG_DESIGNING_HIDE = "preferences_dialog_designing_hide"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		profileId = intent.extras?.getLong(EXTRA_LONG_PROFILE_ID)

		setupActionBar()
		setContentView(R.layout.activity_settings)
		setupDesigningDialog()

		if (savedInstanceState == null) {
			// Create the fragment only when the activity is created for the first time.
			// ie. not after orientation changes
			val fragment = supportFragmentManager.findFragmentByTag(PreferencesFragment.FRAGMENT_TAG)
					?: PreferencesFragment()
			val args = Bundle()
			profileId?.let { args.putLong(EXTRA_LONG_PROFILE_ID, it) }
			fragment.arguments = args

			supportFragmentManager
					.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.framelayout_settings_content, fragment, PreferencesFragment.FRAGMENT_TAG)
					.commit()
		}
	}

	private fun setupDesigningDialog() {
		val prefs = PreferenceManager(this)
		if (!prefs.defaultPrefs.getBoolean(DIALOG_DESIGNING_HIDE, false))
			banner_settings_designing.visibility = View.VISIBLE

		banner_settings_designing.setLeftButtonAction {
			banner_settings_designing.dismiss()

			val editor = prefs.defaultPrefs.edit()
			editor.putBoolean(DIALOG_DESIGNING_HIDE, true)
			editor.apply()
		}
		banner_settings_designing.setRightButtonAction {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SapuSeven/BetterUntis/wiki/Designing"))) // TODO: Move this to a common constants class
		}
	}

	private fun setupActionBar() {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	override fun onPreferenceStartScreen(preferenceFragmentCompat: PreferenceFragmentCompat,
	                                     preferenceScreen: PreferenceScreen): Boolean {
		val fragment = PreferencesFragment()
		val args = Bundle()
		args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
		profileId?.let { args.putLong(EXTRA_LONG_PROFILE_ID, it) }
		fragment.arguments = args

		supportFragmentManager
				.beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.framelayout_settings_content, fragment, preferenceScreen.key)
				.addToBackStack(preferenceScreen.key)
				.commit()
		return true
	}

	class PreferencesFragment : PreferenceFragmentCompat() {
		companion object {
			const val FRAGMENT_TAG = "preference_fragment"
			const val DIALOG_FRAGMENT_TAG = "preference_dialog_fragment"
		}

		private var profileId: Long = 0

		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			profileId = arguments?.getLong(EXTRA_LONG_PROFILE_ID) ?: 0
			// TODO: Show error if profileId == 0
			preferenceManager.sharedPreferencesName = "preferences_$profileId"

			setPreferencesFromResource(R.xml.preferences, rootKey)

			when (rootKey) {
				"preferences_styling" ->
					listOf("preference_theme", "preference_dark_theme", "preference_dark_theme_oled").forEach { key ->
						findPreference(key)?.setOnPreferenceChangeListener { _, _ ->
							activity?.recreate()
							true
						}
					}
				"preferences_notifications" ->
					findPreference("preference_notifications_clear")?.setOnPreferenceChangeListener { _, _ ->
						(context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.cancelAll()
						true
					}
			}
		}

		override fun onPreferenceTreeClick(preference: Preference): Boolean {
			if (preference is ElementPickerPreference) {
				val userDatabase = UserDatabase.createInstance(requireContext())
				val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileId)

				ElementPickerDialog.newInstance(
						timetableDatabaseInterface,
						ElementPickerDialog.Companion.ElementPickerDialogConfig(TimetableDatabaseInterface.Type.valueOf(preference.getSavedType())),
						object : ElementPickerDialog.ElementPickerDialogListener {
							override fun onDialogDismissed(dialog: DialogInterface?) {
								// ignore
							}

							override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?, useOrgId: Boolean) {
								preference.setElement(
										element,
										element?.let {
											timetableDatabaseInterface.getShortName(it.id, TimetableDatabaseInterface.Type.valueOf(it.type))
										} ?: "")
								dialog.dismiss()
							}

							override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
								// positive button not used
							}
						}
				).show(fragmentManager!!, "elementPicker")
			}

			return true
		}

		override fun onDisplayPreferenceDialog(preference: Preference) {
			fragmentManager?.let { manager ->
				if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) return

				when (preference) {
					is AlertPreference -> {
						val f: DialogFragment = AlertPreferenceDialog.newInstance(preference.key)
						f.setTargetFragment(this, 0)
						f.show(manager, DIALOG_FRAGMENT_TAG)
					}
					else -> super.onDisplayPreferenceDialog(preference)
				}
			}
		}
	}
}
