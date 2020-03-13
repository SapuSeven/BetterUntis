package com.sapuseven.untis.wear.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.input.RotaryEncoder
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.sapuseven.untis.wear.R
import kotlin.math.roundToInt

class MainActivity : WearableActivity() {

    private var scrollView: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adjustInset(findViewById(R.id.content))
        scrollView = findViewById(R.id.root)

        findViewById<Button>(R.id.reload).setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.sign_out).setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("signed_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(event)) {
            val delta = -RotaryEncoder.getRotaryAxisValue(event) * RotaryEncoder.getScaledScrollFactor(this)
            scrollView!!.scrollBy(0, delta.roundToInt())
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    private fun adjustInset(layout: View) {
        if (applicationContext.resources.configuration.isScreenRound) {
            val inset = (FACTOR * Resources.getSystem().displayMetrics.widthPixels).toInt()
            layout.setPadding(inset, inset, inset, inset)
        }
    }

    companion object {
        private const val FACTOR = 0.146467f
    }
}
