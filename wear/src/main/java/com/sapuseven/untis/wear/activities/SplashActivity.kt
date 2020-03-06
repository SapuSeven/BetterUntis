package com.sapuseven.untis.wear.activities

import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.preference.PreferenceManager

class SplashActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("edittext_logindatainput_school", "") == "") {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
