package com.sapuseven.untis.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
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
import com.sapuseven.untis.ui.common.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.theme.AppTheme

class ShortcutConfigureActivity : BaseComposeActivity() {
	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		val userDatabase = UserDatabase.createInstance(this)
		val users = userDatabase.getAllUsers()

		setContent {
			AppTheme {
				var showElementPicker by rememberSaveable { mutableStateOf(false) }
				var selectedUserId by rememberSaveable {
					mutableStateOf<Long>(
						users.firstOrNull()?.id ?: -1
					)
				}
				var selectedElement by remember { mutableStateOf<PeriodElement?>(null) }

				var timetableDatabaseInterface by remember {
					mutableStateOf<TimetableDatabaseInterface?>(
						null
					)
				}

				LaunchedEffect(selectedUserId) {
					timetableDatabaseInterface =
						TimetableDatabaseInterface(userDatabase, selectedUserId)
				}

				Scaffold(
					topBar = {
						CenterAlignedTopAppBar(
							title = { Text(stringResource(id = R.string.widget_timetable_link)) },
							navigationIcon = {
								IconButton(onClick = {
									finish()
								}) {
									Icon(
										imageVector = Icons.Outlined.ArrowBack,
										contentDescription = stringResource(id = R.string.all_back)
									)
								}
							},
							actions = {
								ProfileSelectorAction(
									userDatabase = userDatabase,
									currentSelectionId = selectedUserId,
									hideIfSingleProfile = true
								) {
									selectedUserId = it.id ?: -1
								}
							}
						)
					},
				) { innerPadding ->
					Box(
						modifier = Modifier
							.padding(innerPadding)
							.fillMaxSize()
					) {
						Column {
							ListItem(
								headlineText = { Text("Select Timetable") },
								supportingText = selectedElement?.let { item ->
									timetableDatabaseInterface?.getLongName(
										item
									)?.let { { Text(it) } }
								},
								modifier = Modifier.clickable { showElementPicker = true }
							)
						}

						timetableDatabaseInterface?.let {
							if (showElementPicker)
								ElementPickerDialogFullscreen(
									title = { Text(stringResource(id = R.string.widget_configuration)) },
									timetableDatabaseInterface = it,
									onDismiss = { finish() },
									onSelect = { item ->
										showElementPicker = false
										selectedElement = item
										finish()
									}
								)
						}
					}
				}
			}
		}
	}

	private fun setupShortcut(
		timetableDatabaseInterface: TimetableDatabaseInterface,
		user: Long,
		element: PeriodElement?
	) {
		val shortcutIntent = Intent(this, MainActivity::class.java)
			.setAction("")
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("user", user)
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
					//.setActivity(MainActivity)
					.setShortLabel(
						if (element == null) resources.getString(R.string.all_personal)
						else timetableDatabaseInterface.getShortName(
							element.id,
							TimetableDatabaseInterface.Type.valueOf(element.type)
						) ?: "Timetable"
					)
					.setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut))
					.build()
			)
		)
	}
}
