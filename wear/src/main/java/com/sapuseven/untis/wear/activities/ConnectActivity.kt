package com.sapuseven.untis.wear.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sapuseven.untis.wear.R

class ConnectActivity : WearableActivity() {

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            startActivity(Intent(c, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("LOGIN_SUCCESS"))
    }
}
