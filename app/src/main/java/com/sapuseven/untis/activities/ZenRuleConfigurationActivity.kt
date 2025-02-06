package com.sapuseven.untis.activities

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle

class ZenRuleConfigurationActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Set up your UI for configuring the DND rule

		// Retrieve the rule ID from the intent
		val intent = intent
		if (NotificationManager.ACTION_AUTOMATIC_ZEN_RULE == intent.action) {
			val ruleId = intent.getStringExtra(NotificationManager.EXTRA_AUTOMATIC_RULE_ID)
			// Use the ruleId to load and modify the specific AutomaticZenRule
		}
	}
}
