package com.sapuseven.untis.services

import android.app.AutomaticZenRule
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.service.notification.Condition
import androidx.annotation.RequiresApi
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.activities.AutoMuteConfigurationActivity
import com.sapuseven.untis.data.database.entities.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Implementation of the AutoMuteService for API 29 (Q) and above.
 *
 * This implementation utilizes the DND Access API to setup an AutomaticZenRule.
 * It is visible in the system settings and can be managed using a configuration activity.
 *
 * Since this approach is based on conditions (which depend on the user id),
 * it is possible to have multiple rules for different users.
 * When Auto-Mute is enabled for multiple users, each user will have their own schedule
 * and Zen mode will be enabled if any of them are active.
 */
// TODO: Use string resources
@RequiresApi(Build.VERSION_CODES.Q)
class AutoMuteServiceZenRuleImpl @Inject constructor(
	@ApplicationContext private val context: Context,
	private val notificationManager: NotificationManager,
) : AutoMuteService {
	private lateinit var conditionUri: Uri
	private lateinit var ruleId: String
	private lateinit var user: User

	fun setUser(user: User) {
		this.user = user

		conditionUri = Uri.Builder()
			.scheme(Condition.SCHEME)
			.authority(BuildConfig.APPLICATION_ID)
			.appendPath("automute")
			.appendQueryParameter("userId", user.id.toString())
			.build();

		try {
			// Load existing rule, remove duplicates (if any)
			notificationManager.automaticZenRules.filter {
				it.value.conditionId == conditionUri
			}.keys.forEachIndexed { index, key ->
				if (index == 0) {
					ruleId = key
				} else {
					notificationManager.removeAutomaticZenRule(key)
				}
			}
		} catch (e: SecurityException) {
			// Permission was revoked
		}
	}

	fun getRule(ruleId: String?): AutomaticZenRule? {
		return ruleId?.let { notificationManager.getAutomaticZenRule(it) }
	}

	override fun isPermissionGranted(): Boolean =
		notificationManager.isNotificationPolicyAccessGranted

	override fun isAutoMuteEnabled(): Boolean =
		this::ruleId.isInitialized && notificationManager.getAutomaticZenRule(ruleId).isEnabled

	override fun autoMuteEnable() {
		val rule = AutomaticZenRule(
			"School - ${user.getDisplayedName(context)}",
			null,
			ComponentName(context, AutoMuteConfigurationActivity::class.java),
			conditionUri,
			null,
			NotificationManager.INTERRUPTION_FILTER_PRIORITY,
			true
		)

		if (this::ruleId.isInitialized) {
			notificationManager.updateAutomaticZenRule(ruleId, rule)
		} else {
			ruleId = notificationManager.addAutomaticZenRule(rule)
		}
	}

	override fun autoMuteDisable() {
		if (this::ruleId.isInitialized) {
			notificationManager.getAutomaticZenRule(ruleId)?.apply {
				isEnabled = false
			}?.let {
				notificationManager.updateAutomaticZenRule(ruleId, it)
			}
		}
	}

	override fun autoMuteStateOn() {
		notificationManager.setAutomaticZenRuleState(
			ruleId,
			Condition(conditionUri, "Active", Condition.STATE_TRUE)
		)
	}

	override fun autoMuteStateOff() {
		notificationManager.setAutomaticZenRuleState(
			ruleId,
			Condition(conditionUri, "Inactive", Condition.STATE_FALSE)
		)
	}

	fun zenRuleRemove() {
		notificationManager.automaticZenRules
			.filter {
				it.value.conditionId.equals(conditionUri)
			}
			.forEach {
				notificationManager.removeAutomaticZenRule(it.key)
			}
	}
}
