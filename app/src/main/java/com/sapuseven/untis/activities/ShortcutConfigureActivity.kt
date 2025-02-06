package com.sapuseven.untis.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

// TODO
class ShortcutConfigureActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		//val users = userDatabase.userDao().getAll()

		setContent {
			/*AppTheme {
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
								users = userDatabase.userDao().getAll(),
								currentSelectionId = selectedUserId,
								hideIfSingleProfile = true,
								onSelectionChange = {
									selectedUserId = it.id
								}
							)
						}
					)
				}
			}*/
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

		// TODO
		/*setResult(
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
		)*/
	}
}
