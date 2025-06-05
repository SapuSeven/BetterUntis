package com.sapuseven.untis.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat.createShortcutResultIntent
import androidx.core.graphics.drawable.IconCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.helpers.AppTheme
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutConfigureActivity : ComponentActivity() {
	@Inject
	lateinit var userDao: UserDao

	@Inject
	lateinit var masterDataRepository: MasterDataRepository

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				/*AppScaffold(
					modifier = Modifier.fillMaxSize(),
					topBar = {
						TopAppBar(
							title = {
								Text("Add shortcut")
							},
							navigationIcon = {
								IconButton(onClick = { finish() }) {
									Icon(
										imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
										contentDescription = stringResource(id = R.string.all_back)
									)
								}
							}
						)
					},
					bottomBar = {
						BottomAppBar {
							Row(
								horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
								modifier = Modifier
									.fillMaxWidth()
									.padding(16.dp)
							) {
								TextButton(onClick = {}) {
									Text(stringResource(R.string.all_cancel))
								}
								Button(onClick = {}) {
									Text(stringResource(R.string.all_save))
								}
							}
						}
					}
				) {*/
				val users = remember { mutableStateListOf<User>() }
				var selectedUserId by rememberSaveable { mutableLongStateOf(-1L) }

				LaunchedEffect(userDao) {
					userDao.getAllFlow().collect {
						users.clear()
						users.addAll(it)
					}
				}

				ElementPickerDialogFullscreen(
					masterDataRepository = masterDataRepository,
					title = { Text(stringResource(id = R.string.widget_timetable_link)) },
					onDismiss = { finish() },
					onSelect = { element ->
						setupShortcut(selectedUserId, element)
						finish()
					},
					additionalActions = {
						ProfileSelectorAction(
							users = users,
							currentSelectionId = selectedUserId,
							hideIfSingleProfile = true,
							onSelectionChange = {
								selectedUserId = it.id
							}
						)
					}
				)
			}
		}
	}

	private fun setupShortcut(
		userId: Long,
		element: PeriodElement?
	) {
		val shortcutIntent = Intent(this, MainActivity::class.java)
			.setAction("")
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("user", userId)
			.putExtra("type", element?.type?.name)
			.putExtra("id", element?.id)

		setResult(
			RESULT_OK,
			createShortcutResultIntent(
				this,
				ShortcutInfoCompat.Builder(this, "${element?.type}-${element?.id}")
					.setIntent(shortcutIntent)
					.setShortLabel(
						if (element == null) resources.getString(R.string.all_personal)
						else masterDataRepository.getShortName(element, resources.getString(R.string.widget_timetable))
					)
					.setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut))
					.build()
			)
		)
	}
}
