package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.dialogs.ProfileUpdateDialog
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.api.LoginDataInfo
import com.sapuseven.untis.helpers.api.LoginErrorInfo
import com.sapuseven.untis.helpers.api.LoginHelper
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.masterdata.TimeGrid
import kotlinx.android.synthetic.main.activity_logindatainput.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class LoginDataInputActivity : BaseActivity() {
	companion object {
		private const val BACKUP_PREF_NAME = "loginDataInputBackup"

		private const val FRAGMENT_TAG_PROFILE_UPDATE = "profileUpdate"

		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileId"
		const val EXTRA_BOOLEAN_PROFILE_UPDATE = "com.sapuseven.untis.activities.profileupdate"
	}

	private var anonymous: Boolean = false
	private var schoolInfoFromSearch: UntisSchoolInfo? = null

	private var existingUser: UserDatabase.User? = null
	private var existingUserId: Long? = null

	private lateinit var userDatabase: UserDatabase

	override fun onCreate(savedInstanceState: Bundle?) {
		if (intent.hasExtra(EXTRA_LONG_PROFILE_ID)) {
			existingUserId = intent.getLongExtra(EXTRA_LONG_PROFILE_ID, 0)
			preferences = PreferenceManager(this, existingUserId!!)
		}

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_logindatainput)

		userDatabase = UserDatabase.createInstance(this)
		existingUserId?.let { id ->
			existingUser = userDatabase.getUser(id)
			existingUser?.let { user ->
				restoreInput(user)
			}
		} ?: run {
			this.getSharedPreferences(BACKUP_PREF_NAME, Context.MODE_PRIVATE)?.let {
				restoreInput(it)
			}
		}

		title =
			getString(if (existingUserId == null) R.string.logindatainput_title_add else R.string.logindatainput_title_edit)

		button_logindatainput_login?.setOnClickListener {
			validate()?.requestFocus() ?: run { loadData() }
		}

		existingUser?.let { user ->
			button_logindatainput_delete?.visibility = View.VISIBLE
			button_logindatainput_delete?.setOnClickListener {
				deleteProfile(user)
			}
		}

		switch_logindatainput_anonymouslogin?.setOnCheckedChangeListener { _, isChecked ->
			anonymous = isChecked

			textinputlayout_logindatainput_user?.isEnabled = !isChecked
			textinputlayout_logindatainput_key?.isEnabled = !isChecked
		}

		switch_logindatainput_advanced?.setOnCheckedChangeListener { _, isChecked ->
			linearlayout_logindatainput_advanced?.visibility =
				if (isChecked) View.VISIBLE else View.GONE
		}

		val appLinkData = intent.data

		if (appLinkData?.isHierarchical == true) {
			if (appLinkData.scheme == "untis" && appLinkData.host == "setschool") {
				edittext_logindatainput_school?.setText(appLinkData.getQueryParameter("school"))
				edittext_logindatainput_user?.setText(appLinkData.getQueryParameter("user"))
				edittext_logindatainput_key?.setText(appLinkData.getQueryParameter("key"))
			} else {
				appLinkData.getQueryParameter("schoolInfo")
					?.let { schoolInfoFromSearch = getJSON().decodeFromString<UntisSchoolInfo>(it) }

				edittext_logindatainput_school?.setText(schoolInfoFromSearch?.schoolId.toString())
			}
		}

		focusFirstFreeField()

		setElementsEnabled(true)

		if (intent.getBooleanExtra(EXTRA_BOOLEAN_PROFILE_UPDATE, false)) {
			supportFragmentManager
				.beginTransaction()
				.replace(android.R.id.content, ProfileUpdateDialog(), FRAGMENT_TAG_PROFILE_UPDATE)
				.commit()

			loadData()
		}
	}

	private fun validate(): EditText? {
		if (edittext_logindatainput_user?.text?.isEmpty() == true && !anonymous) {
			edittext_logindatainput_user.error =
				getString(R.string.logindatainput_error_field_empty)
			return edittext_logindatainput_user
		}
		if (edittext_logindatainput_school?.text?.isEmpty() == true) {
			edittext_logindatainput_school.error =
				getString(R.string.logindatainput_error_field_empty)
			return edittext_logindatainput_school
		}

		if (switch_logindatainput_advanced.isChecked) {
			if (!edittext_logindatainput_api_url.text.isNullOrBlank()
				&& !Patterns.WEB_URL.matcher(edittext_logindatainput_api_url.text.toString())
					.matches()
			) {
				edittext_logindatainput_api_url.error =
					getString(R.string.logindatainput_error_invalid_url)
				return edittext_logindatainput_api_url
			}
		}
		return null
	}

	private fun focusFirstFreeField() {
		when {
			edittext_logindatainput_school?.text?.isEmpty() == true -> edittext_logindatainput_school as EditText
			edittext_logindatainput_user?.text?.isEmpty() == true -> edittext_logindatainput_user as EditText
			edittext_logindatainput_key?.text?.isEmpty() == true -> edittext_logindatainput_key as EditText
			else -> edittext_logindatainput_user as EditText
		}.requestFocus()
	}

	public override fun onResume() {
		super.onResume()
	}

	public override fun onPause() {
		backupInput(this.getSharedPreferences(BACKUP_PREF_NAME, Context.MODE_PRIVATE))

		super.onPause()
	}

	private fun backupInput(prefs: SharedPreferences) {
		val editor = prefs.edit()
		editor.putString(
			"edittext_logindatainput_school",
			edittext_logindatainput_school?.text.toString()
		)
		editor.putBoolean(
			"switch_logindatainput_anonymouslogin",
			switch_logindatainput_anonymouslogin.isChecked
		)
		editor.putString(
			"edittext_logindatainput_user",
			edittext_logindatainput_user?.text.toString()
		)
		editor.putString(
			"edittext_logindatainput_key",
			edittext_logindatainput_key?.text.toString()
		)
		editor.putString("edittext_logindatainput_proxy_host", getProxyHost())
		editor.apply()
	}

	private fun restoreInput(prefs: SharedPreferences) {
		edittext_logindatainput_school?.setText(
			prefs.getString(
				"edittext_logindatainput_school",
				""
			)
		)
		prefs.getString("edittext_logindatainput_proxy_host", "").let {
			edittext_logindatainput_proxy_host?.setText(it)
			if (it?.isNotBlank() == true) {
				switch_logindatainput_advanced?.isChecked = true
				linearlayout_logindatainput_advanced?.visibility = View.VISIBLE
			}
		}
		anonymous = prefs.getBoolean("switch_logindatainput_anonymouslogin", false)
		switch_logindatainput_anonymouslogin?.isChecked = anonymous
		if (!anonymous) {
			edittext_logindatainput_user?.setText(
				prefs.getString(
					"edittext_logindatainput_user",
					""
				)
			)
			edittext_logindatainput_key?.setText(prefs.getString("edittext_logindatainput_key", ""))
		}
	}

	private fun restoreInput(user: UserDatabase.User) {
		edittext_logindatainput_profilename?.setText(user.profileName)
		if (user.schoolId.isNotBlank()) edittext_logindatainput_school?.setText(user.schoolId)

		user.id?.let { profileId ->
			preferences.prefsForProfile(profileId)
				.getString("preference_connectivity_proxy_host", null)?.let {
					edittext_logindatainput_proxy_host?.setText(it)
					if (it.isNotBlank()) {
						switch_logindatainput_advanced?.isChecked = true
						linearlayout_logindatainput_advanced?.visibility = View.VISIBLE
					}
				}
		}

		anonymous = user.anonymous
		switch_logindatainput_anonymouslogin?.isChecked = anonymous
		edittext_logindatainput_user?.setText(user.user)
		edittext_logindatainput_key?.setText(user.key)
	}

	private fun loadData() {
		imageview_logindatainput_loadingstatusfailed?.visibility = View.GONE
		imageview_logindatainput_loadingstatussuccess?.visibility = View.GONE
		progressbar_logindatainput_loadingstatus?.visibility = View.VISIBLE
		textview_logindatainput_loadingstatus?.visibility = View.VISIBLE

		setElementsEnabled(false)
		GlobalScope.launch(Dispatchers.Main) {
			sendRequest()
		}
	}

	private suspend fun sendRequest() {
		LoginHelper(
			loginData = LoginDataInfo(
				edittext_logindatainput_user?.text.toString(),
				edittext_logindatainput_key?.text.toString(),
				anonymous
			),
			onStatusUpdate = { status ->
				updateLoadingStatus(getString(status))
			},
			onError = { error ->
				val errorMessage = when {
					error.errorCode != null -> ErrorMessageDictionary.getErrorMessage(
						resources,
						error.errorCode,
						error.errorMessage
					)
					error.errorMessageStringRes != null -> getString(
						error.errorMessageStringRes,
						error.errorMessage
					)
					else -> error.errorMessage ?: getString(R.string.all_error)
				}
				stopLoadingAndShowError(errorMessage)
			}).run {
			val schoolInfo = (
					when {
						schoolInfoFromSearch != null -> schoolInfoFromSearch
						switch_logindatainput_advanced.isChecked && !edittext_logindatainput_api_url.text.isNullOrBlank() -> UntisSchoolInfo(
							server = "",
							useMobileServiceUrlAndroid = true,
							useMobileServiceUrlIos = true,
							address = "",
							displayName = edittext_logindatainput_school.text.toString(),
							loginName = edittext_logindatainput_school.text.toString(),
							schoolId = edittext_logindatainput_school.text.toString().toIntOrNull()
								?: 0,
							serverUrl = edittext_logindatainput_api_url.text.toString(),
							mobileServiceUrl = edittext_logindatainput_api_url.text.toString()
						)
						else -> loadSchoolInfo(
							edittext_logindatainput_school?.text.toString()
						)
					}) ?: return@run
			val apiUrl =
				if (switch_logindatainput_advanced.isChecked && !edittext_logindatainput_api_url.text.isNullOrBlank())
					edittext_logindatainput_api_url.text.toString()
				else if (schoolInfo.useMobileServiceUrlAndroid && !schoolInfo.mobileServiceUrl.isNullOrBlank()) schoolInfo.mobileServiceUrl!!
				else Uri.parse(schoolInfo.serverUrl).buildUpon()
					.appendEncodedPath("jsonrpc_intern.do")
					.build().toString()
			val appSharedSecret =
				when {
					loginData.anonymous -> ""
					checkbox_logindatainput_skip_app_secret.isChecked -> loginData.password
					else -> loadAppSharedSecret(apiUrl)
						?: return@run
				}
			val userDataResponse = loadUserData(apiUrl, appSharedSecret) ?: return@run
			val user = UserDatabase.User(
				existingUserId,
				edittext_logindatainput_profilename?.text.toString(),
				apiUrl,
				schoolInfo.schoolId.toString(),
				if (!anonymous) loginData.user else null,
				if (!anonymous) appSharedSecret else null,
				anonymous,
				userDataResponse.masterData.timeGrid ?: TimeGrid.generateDefault(),
				userDataResponse.masterData.timeStamp,
				userDataResponse.userData,
				userDataResponse.settings
			)

			val userId =
				if (existingUserId == null) userDatabase.addUser(user) else userDatabase.editUser(
					user
				)

			userId?.let {
				userDatabase.setAdditionalUserData(userId, userDataResponse.masterData)

				progressbar_logindatainput_loadingstatus?.visibility = View.GONE
				imageview_logindatainput_loadingstatussuccess?.visibility = View.VISIBLE
				textview_logindatainput_loadingstatus?.text =
					getString(R.string.logindatainput_data_loaded)

				preferences.saveProfileId(userId.toLong())

				if (getProxyHost()?.isNotBlank() == true)
					with(preferences.defaultPrefs.edit()) {
						putString("preference_connectivity_proxy_host", getProxyHost())
						apply()
					}

				setResult(Activity.RESULT_OK)
				finish()
			} ?: run {
				onError(LoginErrorInfo(errorMessageStringRes = R.string.logindatainput_adding_user_unknown_error))
			}
		}

		setElementsEnabled(true)
	}

	private fun deleteProfile(user: UserDatabase.User) {
		MaterialAlertDialogBuilder(this)
			.setTitle(getString(R.string.main_dialog_delete_profile_title))
			.setMessage(
				getString(
					R.string.main_dialog_delete_profile_message,
					user.getDisplayedName(applicationContext),
					user.userData.schoolName
				)
			)
			.setNegativeButton(getString(R.string.all_cancel), null)
			.setPositiveButton(getString(R.string.all_delete)) { _, _ ->
				userDatabase.deleteUser(user.id!!)
				preferences.deleteProfile(user.id)
				setResult(RESULT_OK)
				finish()
			}
			.show()
	}

	private fun updateLoadingStatus(msg: String) {
		textview_logindatainput_loadingstatus?.text = msg
	}

	private fun stopLoadingAndShowError(msg: String) {
		updateLoadingStatus(msg)
		progressbar_logindatainput_loadingstatus?.visibility = View.GONE
		imageview_logindatainput_loadingstatusfailed?.visibility = View.VISIBLE
		setElementsEnabled(true)

		supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_PROFILE_UPDATE)?.let {
			supportFragmentManager
				.beginTransaction()
				.remove(it)
				.commit()
		}
	}

	override fun onBackPressed() {
		setElementsEnabled(false)
		super.onBackPressed()
	}

	private fun getProxyHost(): String? =
		if (switch_logindatainput_advanced.isChecked) edittext_logindatainput_proxy_host?.text.toString() else null

	private fun setElementsEnabled(enabled: Boolean) {
		textinputlayout_logindatainput_profilename?.isEnabled = enabled
		textinputlayout_logindatainput_school?.isEnabled = enabled && schoolInfoFromSearch == null
		textinputlayout_logindatainput_user?.isEnabled =
			enabled && switch_logindatainput_anonymouslogin?.isChecked == false
		textinputlayout_logindatainput_key?.isEnabled =
			enabled && switch_logindatainput_anonymouslogin?.isChecked == false
		textinputlayout_logindatainput_proxy_host?.isEnabled = enabled
		button_logindatainput_login?.isEnabled = enabled
		button_logindatainput_delete?.isEnabled = enabled
		switch_logindatainput_anonymouslogin?.isEnabled = enabled
		switch_logindatainput_advanced?.isEnabled = enabled
	}
}
