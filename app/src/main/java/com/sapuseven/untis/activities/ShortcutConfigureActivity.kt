package com.sapuseven.untis.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat.createShortcutResultIntent
import androidx.core.graphics.drawable.IconCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.common.ProfileSelectorAction

class ShortcutConfigureActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		val userDatabase = UserDatabase.createInstance(this)
		val users = userDatabase.getAllUsers()

		setContent {
			AppTheme {
				var selectedUserId by rememberSaveable {
					mutableStateOf(users.firstOrNull()?.id ?: -1)
				}

				var timetableDatabaseInterface by remember {
					mutableStateOf(TimetableDatabaseInterface(userDatabase, selectedUserId))
				}

				LaunchedEffect(selectedUserId) {
					timetableDatabaseInterface =
						TimetableDatabaseInterface(userDatabase, selectedUserId)
				}

				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					ElementPickerDialogFullscreen(
						title = { Text(stringResource(id = R.string.widget_timetable_link)) },
						timetableDatabaseInterface = timetableDatabaseInterface,
						onDismiss = { finish() },
						onSelect = { element ->
							setupShortcut(timetableDatabaseInterface, selectedUserId, element)
							finish()
						},
						additionalActions = {
							ProfileSelectorAction(
								users = userDatabase.getAllUsers(),
								currentSelectionId = selectedUserId,
								hideIfSingleProfile = true,
								onSelectionChange =  {
									selectedUserId = it.id ?: -1
								}
							)
						}
					)
				}
			}
		}
	}

	private fun setupShortcut(
		timetableDatabaseInterface: TimetableDatabaseInterface,
		userId: Long,
		element: PeriodElement?
	) {
		val shortcutIntent = Intent(this, MainActivity::class.java)
			.setAction("")
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("user", userId)
			.putExtra("type", element?.type)
			.putExtra("id", element?.id)
			.putExtra("orgId", element?.orgId)
			.putExtra("useOrgId", false)

		setResult(
			RESULT_OK,
			createShortcutResultIntent(
				this,
				ShortcutInfoCompat.Builder(
					this,
					"${element?.type}-${element?.id}-${element?.orgId}"
				)
					.setIntent(shortcutIntent)
					.setShortLabel(
						if (element == null) resources.getString(R.string.all_personal)
						else timetableDatabaseInterface.getShortName(
							element.id,
							TimetableDatabaseInterface.Type.valueOf(element.type)
						).ifBlank { resources.getString(R.string.widget_timetable) }
					)
					.setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut))
					.build()
			)
		)
	}
}
