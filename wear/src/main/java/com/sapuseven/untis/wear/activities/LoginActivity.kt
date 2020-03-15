package com.sapuseven.untis.wear.activities

import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import com.sapuseven.untis.R

class LoginActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.connect).setOnClickListener {
            startActivity(Intent(this, ConnectActivity::class.java))
            finish()
        }
    }
}
