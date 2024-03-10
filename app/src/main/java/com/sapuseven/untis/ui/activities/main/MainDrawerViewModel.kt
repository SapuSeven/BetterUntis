package com.sapuseven.untis.ui.activities.main

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.modules.UserManager
import com.sapuseven.untis.ui.models.NavItemShortcut
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import javax.inject.Inject

@HiltViewModel
class MainDrawerViewModel @Inject constructor(
	private val userManager: UserManager,
	private val userDao: UserDao
) : ViewModel() {
	var displayedElement by mutableStateOf<PeriodElement?>(null)

	var enableDrawerGestures: Boolean = true
		private set

	var bookmarkDeleteDialog by mutableStateOf<TimetableBookmark?>(null)
		private set

	val user = userManager.activeUser
	fun onShortcutItemClick(
		item: NavItemShortcut,
		shortcutLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
	) {
		/*Log.i("Sentry", "Drawer onClick: ${item}")
		Breadcrumb().apply {
			category = "ui.drawer.click"
			level = SentryLevel.INFO
			setData("id", item.id)
			setData("label", item.label)
			Sentry.addBreadcrumb(this)
		}

		if (item.target == null) {
			try {
				contextActivity.startActivity(
					contextActivity.packageManager.getLaunchIntentForPackage(
						MainActivity.MESSENGER_PACKAGE_NAME
					)
				)
			} catch (e: Exception) {
				try {
					contextActivity.startActivity(
						Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
						)
					)
				} catch (e: Exception) {
					contextActivity.startActivity(
						Intent(
							Intent.ACTION_VIEW,
							Uri.parse("https://play.google.com/store/apps/details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
						)
					)
				}
			}
		} else {
			shortcutLauncher.launch(
				Intent(
					contextActivity,
					item.target
				).apply {
					contextActivity.putUserIdExtra(this, user.id)
					contextActivity.putBackgroundColorExtra(this)
				}
			)
		}*/
	}

	fun createBookmark(item: PeriodElement): Boolean {
		/*val newBookmark = TimetableBookmark(
			elementId = item.id,
			elementType = TimetableDatabaseInterface.Type.valueOf(item.type).name,
			displayName = timetableDatabaseInterface.getLongName(item)
		)

		if (user.bookmarks.contains(newBookmark))
			Toast
				.makeText(
					contextActivity,
					"Bookmark already exists",
					Toast.LENGTH_LONG
				) // TODO: Extract string resource
				.show()
		else {
			user.bookmarks = user.bookmarks.plus(newBookmark)
			userDatabase.userDao().update(user)
			return true
		}*/

		return false
	}

	fun deleteBookmark(bookmark: TimetableBookmark) {
		user.value?.let { user ->
			user.bookmarks = user.bookmarks.minus(bookmark)
			userDao.update(user)
		}
		dismissBookmarkDeleteDialog()
	}

	fun showBookmarkDeleteDialog(bookmark: TimetableBookmark) {
		bookmarkDeleteDialog = bookmark
	}

	fun dismissBookmarkDeleteDialog() {
		bookmarkDeleteDialog = null
	}

	fun isPersonalTimetableDisplayed() = true//displayedElement == personalTimetable?.first
}
