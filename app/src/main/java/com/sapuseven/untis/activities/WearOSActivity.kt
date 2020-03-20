package com.sapuseven.untis.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.*
import com.sapuseven.untis.R

class WearOSActivity : BaseActivity() {

    companion object {
        private const val BACKUP_PREF_NAME = "loginDataInputBackup"
        private const val UNTIS_LOGIN = "/untis_login"
        private const val SUCCESS: Byte = 0x01
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            if (intent.getByteExtra("message", 0x00) == SUCCESS)
                statusImg!!.setImageResource(R.drawable.all_check)
            else
                statusImg!!.setImageResource(R.drawable.all_failed)
        }
    }

    var statusImg: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_os)
        statusImg = findViewById(R.id.status)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("LOGIN_SUCCESS"))
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
}
