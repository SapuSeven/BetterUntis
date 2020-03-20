package com.sapuseven.untis.wear.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_HOST
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_PATH
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import com.sapuseven.untis.models.untis.response.AppSharedSecretResponse
import com.sapuseven.untis.models.untis.response.UserDataResponse
import com.sapuseven.untis.models.untis.response.UserDataResult
import com.sapuseven.untis.wear.helpers.ErrorHandling.ACQUIRING_APP_SHARED_SECRET_FAILED
import com.sapuseven.untis.wear.helpers.ErrorHandling.ACQUIRING_USER_DATA_FAILED
import com.sapuseven.untis.wear.helpers.ErrorHandling.ADDING_USER_FAILED
import com.sapuseven.untis.wear.helpers.ErrorHandling.handleError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ConnectActivity : WearableActivity() {

    companion object {
        private const val UNTIS_SUCCESS = "/untis_success"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(c)
            sendRequest(
                    prefs.getString("edittext_logindatainput_school", "0")?.toInt() ?: 0,
                    prefs.getString("edittext_logindatainput_user", "") ?: "",
                    prefs.getString("edittext_logindatainput_key", "") ?: "",
                    prefs.getBoolean("switch_logindatainput_anonymouslogin", false)
            )
        }
    }

    private var anonymous: Boolean = false
    private var schoolInfo: UntisSchoolInfo? = null

    private var api: UntisRequest = UntisRequest()
    private var existingUserId: Long? = null

    private lateinit var userDatabase: UserDatabase

    private suspend fun acquireAppSharedSecret(schoolId: Int, user: String, password: String): String? {
        val query = UntisRequest.UntisRequestQuery()

        query.url = schoolInfo?.let {
            if (it.useMobileServiceUrlAndroid) it.mobileServiceUrl
            else null
        } ?: (DEFAULT_WEBUNTIS_PROTOCOL + DEFAULT_WEBUNTIS_HOST + DEFAULT_WEBUNTIS_PATH + schoolId)

        query.data.method = UntisApiConstants.METHOD_GET_APP_SHARED_SECRET
        query.data.params = listOf(AppSharedSecretParams(user, password))

        val appSharedSecretResult = api.request(query)

        appSharedSecretResult.fold({ data ->
            val untisResponse = getJSON().parse(AppSharedSecretResponse.serializer(), data)

            if (untisResponse.result.isNullOrEmpty())
                handleError(this, ACQUIRING_APP_SHARED_SECRET_FAILED)
            else
                return untisResponse.result
        }, { error ->
            handleError(this, ACQUIRING_APP_SHARED_SECRET_FAILED)
        })
        return null
    }

    private suspend fun acquireUserData(schoolId: Int, user: String, key: String?): UserDataResult? {
        val query = UntisRequest.UntisRequestQuery()

        query.url = schoolInfo?.let {
            if (it.useMobileServiceUrlAndroid) it.mobileServiceUrl
            else null
        } ?: (DEFAULT_WEBUNTIS_PROTOCOL + DEFAULT_WEBUNTIS_HOST + DEFAULT_WEBUNTIS_PATH + schoolId)
        query.data.method = UntisApiConstants.METHOD_GET_USER_DATA

        if (anonymous)
            query.data.params = listOf(UserDataParams(UntisAuthentication.createAuthObject()))
        else {
            if (key == null) return null
            query.data.params = listOf(UserDataParams(UntisAuthentication.createAuthObject(user, key)))
        }

        val userDataResult = api.request(query)

        userDataResult.fold({ data ->
            val untisResponse = getJSON().parse(UserDataResponse.serializer(), data)

            if (untisResponse.result != null) {
                return untisResponse.result
            } else {
                handleError(this, ACQUIRING_USER_DATA_FAILED)
            }
        }, { error ->
            handleError(this, ACQUIRING_USER_DATA_FAILED)
        })

        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        userDatabase = UserDatabase.createInstance(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("LOGIN_SUCCESS"))
    }

    private fun sendRequest(schoolId: Int, username: String, password: String, anonymous: Boolean) = GlobalScope.launch(Dispatchers.Main) {
        val appSharedSecret: String? = if (anonymous) null else acquireAppSharedSecret(schoolId, username, password)
        if (!anonymous && appSharedSecret == null) return@launch

        acquireUserData(schoolId, username, appSharedSecret)?.let { response ->
            val user = UserDatabase.User(
                    existingUserId,
                    if (schoolInfo?.useMobileServiceUrlAndroid == true) schoolInfo?.mobileServiceUrl else null,
                    schoolId,
                    if (!anonymous) username else null,
                    if (!anonymous) appSharedSecret else null,
                    anonymous,
                    response.masterData.timeGrid,
                    response.masterData.timeStamp,
                    response.userData,
                    response.settings
            )
            val userId = userDatabase.addUser(user)

            userId?.let {
                userDatabase.setAdditionalUserData(userId, response.masterData)
                com.sapuseven.untis.helpers.config.PreferenceManager(this@ConnectActivity).saveProfileId(userId.toLong())
            } ?: run {
                handleError(this@ConnectActivity, ADDING_USER_FAILED)
            }
        }

        SendMessage(this@ConnectActivity).start()
        startActivity(Intent(this@ConnectActivity, MainActivity::class.java))
        finish()
    }

    internal class SendMessage(private val c: Context) : Thread() {

        override fun run() {
            val nodeListTask: Task<List<Node>> = Wearable.getNodeClient(c.applicationContext).connectedNodes
            try {
                val nodes: List<Node> = Tasks.await(nodeListTask)
                nodes.forEach {
                    val sendMessageTask: Task<Int> = Wearable.getMessageClient(c).sendMessage(it.id, UNTIS_SUCCESS, byteArrayOf(0x01))
                    try {
                        val result: Int = Tasks.await(sendMessageTask)
                    } catch (e: Exception) { }
                }
            } catch (e: Exception) { }
        }
    }
}
