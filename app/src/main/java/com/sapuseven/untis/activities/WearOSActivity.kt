package com.sapuseven.untis.activities

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import com.google.android.gms.wearable.*
import com.sapuseven.untis.R

class WearOSActivity : BaseActivity(), MessageClient.OnMessageReceivedListener {

    companion object {
        private const val BACKUP_PREF_NAME = "loginDataInputBackup"
        private const val UNTIS_LOGIN = "/untis_login"
        private const val UNTIS_SUCCESS = "/untis_success"
    }

    var statusImg: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_os)
        statusImg = findViewById(R.id.status)
    }

    override fun onResume() {
        super.onResume()

        val prefs = this.getSharedPreferences(BACKUP_PREF_NAME, Context.MODE_PRIVATE)
        val putDataMapRequest = PutDataMapRequest.create(UNTIS_LOGIN)
        val map = putDataMapRequest.dataMap
        map.putString("edittext_logindatainput_key", prefs.getString("edittext_logindatainput_key", ""))
        map.putString("edittext_logindatainput_school", prefs.getString("edittext_logindatainput_school", ""))
        map.putString("edittext_logindatainput_user", prefs.getString("edittext_logindatainput_user", ""))
        map.putBoolean("switch_logindatainput_anonymouslogin", prefs.getBoolean("switch_logindatainput_anonymouslogin", false))
        val request = putDataMapRequest.asPutDataRequest()
        request.setUrgent()
        Wearable.getDataClient(this).putDataItem(request)
    }

    //TODO: Does not work
    override fun onMessageReceived(p0: MessageEvent) {
        if (p0.path == UNTIS_SUCCESS) {
            statusImg!!.setImageResource(R.drawable.all_check)
        }
    }
}
