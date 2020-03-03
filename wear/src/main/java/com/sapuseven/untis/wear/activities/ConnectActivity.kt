package com.sapuseven.untis.wear.activities

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import com.sapuseven.untis.wear.R

class ConnectActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
    }
}
