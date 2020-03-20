package com.sapuseven.untis.services

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearMessageService : WearableListenerService() {

    companion object {
        private const val UNTIS_SUCCESS = "/untis_success"
    }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent!!.path == UNTIS_SUCCESS) {
            val message = messageEvent.data[0]
            val messageIntent = Intent("LOGIN_SUCCESS")
            messageIntent.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}