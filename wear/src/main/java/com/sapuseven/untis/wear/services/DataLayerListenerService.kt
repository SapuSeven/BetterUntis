package com.sapuseven.untis.wear.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.common.data.FreezableUtils
import com.google.android.gms.wearable.*

class DataLayerListenerService : WearableListenerService() {

    companion object {
        private const val UNTIS_LOGIN = "/untis_login"
        private const val UNTIS_SUCCESS = "/untis_success"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val events = FreezableUtils.freezeIterable(dataEvents)
        events.forEach {
            val item = it.dataItem
            if (item.uri.path == UNTIS_LOGIN) {
                val map = DataMapItem.fromDataItem(item).dataMap
                Log.e("LOGIN", map.getString("edittext_logindatainput_key"))
                Log.e("LOGIN", map.getString("edittext_logindatainput_school"))
                Log.e("LOGIN", map.getString("edittext_logindatainput_user"))
                Log.e("LOGIN", map.getBoolean("switch_logindatainput_anonymouslogin").toString())

                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putString("edittext_logindatainput_key", map.getString("edittext_logindatainput_key"))
                editor.putString("edittext_logindatainput_school", map.getString("edittext_logindatainput_school"))
                editor.putString("edittext_logindatainput_user", map.getString("edittext_logindatainput_user"))
                editor.putBoolean("switch_logindatainput_anonymouslogin", map.getBoolean("switch_logindatainput_anonymouslogin"))
                editor.putBoolean("signed_in", true)
                editor.apply()

                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("LOGIN_SUCCESS"))

                //TODO: Does not work
                Wearable.getMessageClient(this).sendMessage(item.uri.host ?:"", UNTIS_SUCCESS, item.uri.toString().toByteArray())
            }
        }
    }
}