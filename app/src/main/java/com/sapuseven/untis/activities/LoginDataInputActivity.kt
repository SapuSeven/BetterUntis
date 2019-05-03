package com.sapuseven.untis.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_PROTOCOL
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_PATH
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.User
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import com.sapuseven.untis.models.untis.response.AppSharedSecretResponse
import com.sapuseven.untis.models.untis.response.UserDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class LoginDataInputActivity : BaseActivity() {
	companion object {
		private const val BACKUP_PREF_NAME = "loginDataInputBackup"
	}

	private var pbLoadingStatus: ProgressBar? = null
	private var ivLoadingStatusSuccess: ImageView? = null
	private var ivLoadingStatusFailed: ImageView? = null
	private var tvLoadingStatus: TextView? = null

	private var etUrl: AutoCompleteTextView? = null
	private var etSchool: TextInputEditText? = null
	private var etUser: TextInputEditText? = null
	private var etUserContainer: TextInputLayout? = null
	private var etKey: TextInputEditText? = null
	private var etKeyContainer: TextInputLayout? = null
	private var btnLogin: Button? = null
	private var sAnonymousLogin: Switch? = null

	private var anonymous: Boolean = false
	private var schoolInfo: UntisSchoolInfo? = null

	private var api: UntisRequest = UntisRequest()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_logindatainput)

		btnLogin = findViewById(R.id.button_logindatainput_login)
		etUrl = findViewById(R.id.edittext_logindatainput_url)
		etSchool = findViewById(R.id.edittext_logindatainput_school)
		etUser = findViewById(R.id.edittext_logindatainput_user)
		etUserContainer = findViewById(R.id.textinputlayout_logindatainput_user)
		etKey = findViewById(R.id.edittext_logindatainput_key)
		etKeyContainer = findViewById(R.id.textinputlayout_logindatainput_key)
		sAnonymousLogin = findViewById(R.id.switch_logindatainput_anonymouslogin)

		btnLogin?.setOnClickListener {
			var error: EditText? = null
			if (etUser?.text?.isEmpty() == true && !anonymous) {
				etUser?.error = getString(R.string.logindatainput_error_field_empty)
				error = etUser
			}
			if (etSchool?.text?.isEmpty() == true) {
				etSchool?.error = getString(R.string.logindatainput_error_field_empty)
				error = etSchool
			}
			if (etUrl?.text?.isEmpty() == true) {
				etUrl?.error = getString(R.string.logindatainput_error_field_empty)
				error = etUrl
			} else if (!Patterns.DOMAIN_NAME.matcher(etUrl?.text).matches()) {
				etUrl?.error = getString(R.string.logindatainput_error_invalid_url)
				error = etUrl
			}

			if (error == null)
				loadData()
			else
				error.requestFocus()
		}

		sAnonymousLogin?.setOnCheckedChangeListener { _, isChecked ->
			anonymous = isChecked

			etUserContainer?.isEnabled = !isChecked
			etKeyContainer?.isEnabled = !isChecked
		}

		pbLoadingStatus = findViewById(R.id.progressbar_logindatainput_loadingstatus)
		ivLoadingStatusSuccess = findViewById(R.id.imageview_logindatainput_loadingstatussuccess)
		ivLoadingStatusFailed = findViewById(R.id.imageview_logindatainput_loadingstatusfailed)
		tvLoadingStatus = findViewById(R.id.textview_logindatainput_loadingstatus)

		val servers = resources.getStringArray(R.array.logindatainput_webuntis_servers)
		val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, servers)
		etUrl?.setAdapter(adapter)

		val prefs = this.getSharedPreferences(BACKUP_PREF_NAME, Context.MODE_PRIVATE)
		prefs?.let {
			restoreInput(prefs)
		}

		val appLinkData = intent.data

		if (appLinkData?.isHierarchical == true) {
			if (appLinkData.scheme == "untis" && appLinkData.host == "setschool") {
				etUrl?.setText(appLinkData.getQueryParameter("url"))
				etSchool?.setText(appLinkData.getQueryParameter("school"))
				etUser?.setText(appLinkData.getQueryParameter("user"))
				etKey?.setText(appLinkData.getQueryParameter("key"))
			} else {
				appLinkData.getQueryParameter("schoolInfo")?.let { schoolInfo = getJSON().parse(UntisSchoolInfo.serializer(), it) }

				etUrl?.setText(schoolInfo?.server)
				etSchool?.setText(schoolInfo?.loginName)
			}
		}

		focusFirstFreeField()

		setElementsEnabled(true)
	}

	private fun focusFirstFreeField() {
		when {
			etUrl?.text?.isEmpty() == true -> etUrl
			etSchool?.text?.isEmpty() == true -> etSchool
			etUser?.text?.isEmpty() == true -> etUser
			etKey?.text?.isEmpty() == true -> etKey
			else -> etUser
		}?.requestFocus()
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
		editor.putString("etUrl", etUrl?.text.toString())
		editor.putString("etSchool", etSchool?.text.toString())
		editor.putString("etUser", etUser?.text.toString())
		editor.putString("etKey", etKey?.text.toString())
		editor.apply()
	}

	private fun restoreInput(prefs: SharedPreferences) {
		etUrl?.setText(prefs.getString("etUrl", ""))
		etSchool?.setText(prefs.getString("etSchool", ""))
		etUser?.setText(prefs.getString("etUser", ""))
		etKey?.setText(prefs.getString("etKey", ""))
	}

	private fun loadData() {
		ivLoadingStatusFailed?.visibility = View.GONE
		ivLoadingStatusSuccess?.visibility = View.GONE
		pbLoadingStatus?.visibility = View.VISIBLE
		tvLoadingStatus?.visibility = View.VISIBLE

		sendRequest()
	}

	private suspend fun acquireAppSharedSecret(): String? {
		updateLoadingStatus(getString(R.string.logindatainput_aquiring_app_secret))

		val query = UntisRequest.UntisRequestQuery()

		val user = etUser?.text.toString()
		val key = etKey?.text.toString()

		query.url = schoolInfo?.mobileServiceUrl
				?: (DEFAULT_PROTOCOL + etUrl?.text.toString() + DEFAULT_WEBUNTIS_PATH)
		query.school = schoolInfo?.loginName ?: etSchool?.text.toString()
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
			val user = etUser?.text.toString()

			query.url = schoolInfo?.mobileServiceUrl
					?: (DEFAULT_PROTOCOL + etUrl?.text.toString() + DEFAULT_WEBUNTIS_PATH)
			query.school = schoolInfo?.loginName ?: etSchool?.text.toString()
			query.data.method = UntisApiConstants.METHOD_GET_USER_DATA

			if (anonymous)
				query.data.params = listOf(UserDataParams(UntisAuthentication.getAnonymousAuthObject()))
			else {
				appSharedSecret = acquireAppSharedSecret()
				if (appSharedSecret == null)
					return@launch
				if (appSharedSecret.isEmpty())
					appSharedSecret = etKey?.text.toString()
				query.data.params = listOf(UserDataParams(UntisAuthentication.getAuthObject(user, appSharedSecret)))
			}

			updateLoadingStatus(getString(R.string.logindatainput_loading_user_data))

			val userDataResult = api.request(query)

			userDataResult.fold({ data ->
				val untisResponse = getJSON().parse(UserDataResponse.serializer(), data)

				if (untisResponse.result != null) {
					val userDatabase = UserDatabase.createInstance(this@LoginDataInputActivity)
					val userId = userDatabase.addUser(User(
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

						pbLoadingStatus?.visibility = View.GONE
						ivLoadingStatusSuccess?.visibility = View.VISIBLE
						tvLoadingStatus?.text = getString(R.string.logindatainput_data_loaded)
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
		tvLoadingStatus?.text = msg
	}

	private fun stopLoadingAndShowError(msg: String) {
		updateLoadingStatus(msg)
		pbLoadingStatus?.visibility = View.GONE
		ivLoadingStatusFailed?.visibility = View.VISIBLE
		setElementsEnabled(true)
	}

	override fun onBackPressed() {
		btnLogin?.isEnabled = false
		super.onBackPressed()
	}

	private fun setElementsEnabled(enabled: Boolean) {
		etUrl?.isEnabled = enabled && schoolInfo == null
		etSchool?.isEnabled = enabled && schoolInfo == null
		etUser?.isEnabled = enabled
		etKey?.isEnabled = enabled
		btnLogin?.isEnabled = enabled
		sAnonymousLogin?.isEnabled = enabled
	}
}
