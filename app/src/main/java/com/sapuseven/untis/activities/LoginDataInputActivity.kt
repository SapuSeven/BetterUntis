package com.sapuseven.untis.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_PROTOCOL
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_PATH
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import com.sapuseven.untis.models.untis.response.AppSharedSecretResponse
import com.sapuseven.untis.models.untis.response.UserDataResponse
import kotlinx.android.synthetic.main.activity_logindatainput.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class LoginDataInputActivity : BaseActivity() {
	companion object {
		private const val BACKUP_PREF_NAME = "loginDataInputBackup"
	}

	private var anonymous: Boolean = false
	private var schoolInfo: UntisSchoolInfo? = null

	private var api: UntisRequest = UntisRequest()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_logindatainput)

		button_logindatainput_login?.setOnClickListener {
			var error: EditText? = null
			if (edittext_logindatainput_user?.text?.isEmpty() == true && !anonymous) {
				edittext_logindatainput_user?.error = getString(R.string.logindatainput_error_field_empty)
				error = edittext_logindatainput_user
			}
			if (edittext_logindatainput_school?.text?.isEmpty() == true) {
				edittext_logindatainput_school?.error = getString(R.string.logindatainput_error_field_empty)
				error = edittext_logindatainput_school
			}
			if (edittext_logindatainput_url?.text?.isEmpty() == true) {
				edittext_logindatainput_url?.error = getString(R.string.logindatainput_error_field_empty)
				error = edittext_logindatainput_url
			} else if (!Patterns.DOMAIN_NAME.matcher(edittext_logindatainput_url?.text).matches()) {
				edittext_logindatainput_url?.error = getString(R.string.logindatainput_error_invalid_url)
				error = edittext_logindatainput_url
			}

			if (error == null)
				loadData()
			else
				error.requestFocus()
		}

		switch_logindatainput_anonymouslogin?.setOnCheckedChangeListener { _, isChecked ->
			anonymous = isChecked

			textinputlayout_logindatainput_user?.isEnabled = !isChecked
			textinputlayout_logindatainput_key?.isEnabled = !isChecked
		}

		val servers = resources.getStringArray(R.array.logindatainput_webuntis_servers)
		val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, servers)
		edittext_logindatainput_url?.setAdapter(adapter)

		val prefs = this.getSharedPreferences(BACKUP_PREF_NAME, Context.MODE_PRIVATE)
		prefs?.let {
			restoreInput(prefs)
		}

		val appLinkData = intent.data

		if (appLinkData?.isHierarchical == true) {
			if (appLinkData.scheme == "untis" && appLinkData.host == "setschool") {
				edittext_logindatainput_url?.setText(appLinkData.getQueryParameter("url"))
				edittext_logindatainput_school?.setText(appLinkData.getQueryParameter("school"))
				edittext_logindatainput_user?.setText(appLinkData.getQueryParameter("user"))
				edittext_logindatainput_key?.setText(appLinkData.getQueryParameter("key"))
			} else {
				appLinkData.getQueryParameter("schoolInfo")?.let { schoolInfo = getJSON().parse(UntisSchoolInfo.serializer(), it) }

				edittext_logindatainput_url?.setText(schoolInfo?.server)
				edittext_logindatainput_school?.setText(schoolInfo?.loginName)
			}
		}

		focusFirstFreeField()

		setElementsEnabled(true)
	}

	private fun focusFirstFreeField() {
		when {
			edittext_logindatainput_url?.text?.isEmpty() == true -> edittext_logindatainput_url as EditText
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
		editor.putString("edittext_logindatainput_url", edittext_logindatainput_url?.text.toString())
		editor.putString("edittext_logindatainput_school", edittext_logindatainput_school?.text.toString())
		editor.putBoolean("switch_logindatainput_anonymouslogin", switch_logindatainput_anonymouslogin.isChecked)
		editor.putString("edittext_logindatainput_user", edittext_logindatainput_user?.text.toString())
		editor.putString("edittext_logindatainput_key", edittext_logindatainput_key?.text.toString())
		editor.apply()
	}

	private fun restoreInput(prefs: SharedPreferences) {
		edittext_logindatainput_url?.setText(prefs.getString("edittext_logindatainput_url", ""))
		edittext_logindatainput_school?.setText(prefs.getString("edittext_logindatainput_school", ""))
		switch_logindatainput_anonymouslogin?.isChecked = prefs.getBoolean("switch_logindatainput_anonymouslogin", false)
		textinputlayout_logindatainput_user?.isEnabled = switch_logindatainput_anonymouslogin?.isChecked == false
		textinputlayout_logindatainput_key?.isEnabled = switch_logindatainput_anonymouslogin?.isChecked == false
		edittext_logindatainput_user?.setText(prefs.getString("edittext_logindatainput_user", ""))
		edittext_logindatainput_key?.setText(prefs.getString("edittext_logindatainput_key", ""))
	}

	private fun loadData() {
		imageview_logindatainput_loadingstatusfailed?.visibility = View.GONE
		imageview_logindatainput_loadingstatussuccess?.visibility = View.GONE
		progressbar_logindatainput_loadingstatus?.visibility = View.VISIBLE
		textview_logindatainput_loadingstatus?.visibility = View.VISIBLE

		sendRequest()
	}

	private suspend fun acquireAppSharedSecret(): String? {
		updateLoadingStatus(getString(R.string.logindatainput_aquiring_app_secret))

		val query = UntisRequest.UntisRequestQuery()

		val user = edittext_logindatainput_user?.text.toString()
		val key = edittext_logindatainput_key?.text.toString()

		query.url = schoolInfo?.mobileServiceUrl
				?: (DEFAULT_PROTOCOL + edittext_logindatainput_url?.text.toString() + DEFAULT_WEBUNTIS_PATH)
		query.school = schoolInfo?.loginName ?: edittext_logindatainput_school?.text.toString()
		query.data.method = UntisApiConstants.METHOD_GET_APP_SHARED_SECRET
		query.data.params = listOf(AppSharedSecretParams(user, key))

		val appSharedSecretResult = api.request(query)

		appSharedSecretResult.fold({ data ->
			val untisResponse = getJSON().parse(AppSharedSecretResponse.serializer(), data)

			if (untisResponse.error?.code == ErrorMessageDictionary.ERROR_CODE_INVALID_CREDENTIALS)
				return ""
			if (untisResponse.result.isNullOrEmpty())
				stopLoadingAndShowError(ErrorMessageDictionary.getErrorMessage(resources, untisResponse.error?.code))
			else
				return untisResponse.result
		}, { error ->
			updateLoadingStatus(error.toString())
			stopLoadingAndShowError(when (error.exception) {
				is UnknownHostException -> ErrorMessageDictionary.getErrorMessage(resources, ErrorMessageDictionary.ERROR_CODE_NO_SERVER_FOUND)
				else -> ErrorMessageDictionary.getErrorMessage(resources, null)
			})
		})

		return null
	}

	private fun sendRequest() {
		GlobalScope.launch(Dispatchers.Main) {
			setElementsEnabled(false)

			val query = UntisRequest.UntisRequestQuery()

			var appSharedSecret: String? = null
			val user = edittext_logindatainput_user?.text.toString()

			query.url = schoolInfo?.mobileServiceUrl
					?: (DEFAULT_PROTOCOL + edittext_logindatainput_url?.text.toString() + DEFAULT_WEBUNTIS_PATH)
			query.school = schoolInfo?.loginName ?: edittext_logindatainput_school?.text.toString()
			query.data.method = UntisApiConstants.METHOD_GET_USER_DATA

			if (anonymous)
				query.data.params = listOf(UserDataParams(UntisAuthentication.getAnonymousAuthObject()))
			else {
				appSharedSecret = acquireAppSharedSecret()
				if (appSharedSecret == null)
					return@launch
				if (appSharedSecret.isEmpty())
					appSharedSecret = edittext_logindatainput_key?.text.toString()
				query.data.params = listOf(UserDataParams(UntisAuthentication.getAuthObject(user, appSharedSecret)))
			}

			updateLoadingStatus(getString(R.string.logindatainput_loading_user_data))

			val userDataResult = api.request(query)

			userDataResult.fold({ data ->
				val untisResponse = getJSON().parse(UserDataResponse.serializer(), data)

				if (untisResponse.result != null) {
					val userDatabase = UserDatabase.createInstance(this@LoginDataInputActivity)
					val userId = userDatabase.addUser(UserDatabase.User(
							-1,
							query.url,
							schoolInfo?.mobileServiceUrl,
							query.school,
							user,
							appSharedSecret,
							anonymous,
							untisResponse.result.masterData.timeGrid,
							untisResponse.result.masterData.timeStamp,
							untisResponse.result.userData,
							untisResponse.result.settings
					))

					userId?.let {
						userDatabase.setAdditionalUserData(userId, untisResponse.result.masterData)

						progressbar_logindatainput_loadingstatus?.visibility = View.GONE
						imageview_logindatainput_loadingstatussuccess?.visibility = View.VISIBLE
						textview_logindatainput_loadingstatus?.text = getString(R.string.logindatainput_data_loaded)
						finish()

						// TODO: Save userId in the defaultPrefs of my PreferenceManager

						return@fold
					} ?: run {
						stopLoadingAndShowError(String.format(getString(R.string.logindatainput_adding_user_unknown_error)))
					}
				} else {
					stopLoadingAndShowError(ErrorMessageDictionary.getErrorMessage(resources, untisResponse.error?.code))
				}

				setElementsEnabled(true)
			}, { error ->
				stopLoadingAndShowError(getString(R.string.logindatainput_error_generic, error.exception))
			})
		}
	}

	private fun updateLoadingStatus(msg: String) {
		textview_logindatainput_loadingstatus?.text = msg
	}

	private fun stopLoadingAndShowError(msg: String) {
		updateLoadingStatus(msg)
		progressbar_logindatainput_loadingstatus?.visibility = View.GONE
		imageview_logindatainput_loadingstatusfailed?.visibility = View.VISIBLE
		setElementsEnabled(true)
	}

	override fun onBackPressed() {
		button_logindatainput_login?.isEnabled = false
		super.onBackPressed()
	}

	private fun setElementsEnabled(enabled: Boolean) {
		textinputlayout_logindatainput_url?.isEnabled = enabled && schoolInfo == null
		textinputlayout_logindatainput_school?.isEnabled = enabled && schoolInfo == null
		textinputlayout_logindatainput_user?.isEnabled = enabled
		textinputlayout_logindatainput_key?.isEnabled = enabled
		button_logindatainput_login?.isEnabled = enabled
		switch_logindatainput_anonymouslogin?.isEnabled = enabled
	}
}
