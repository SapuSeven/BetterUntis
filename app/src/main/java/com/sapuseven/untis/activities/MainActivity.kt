package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.dialogs.DatePickerDialog
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.dialogs.TimetableItemDetailsDialog
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.notifications.StartupReceiver
import com.sapuseven.untis.preferences.ElementPickerPreference
import kotlinx.android.synthetic.main.activity_main_content.*
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class MainActivity :
		BaseActivity(),
		NavigationView.OnNavigationItemSelectedListener,
		MonthLoader.MonthChangeListener<TimegridItem>,
		EventClickListener<TimegridItem>,
		TopLeftCornerClickListener,
		TimetableDisplay,
		TimetableItemDetailsDialog.TimetableItemDetailsDialogListener,
		ElementPickerDialog.ElementPickerDialogListener {

	companion object {
		private const val MINUTE_MILLIS: Int = 60 * 1000
		private const val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
		private const val DAY_MILLIS: Int = 24 * HOUR_MILLIS

		private const val REQUEST_CODE_ROOM_FINDER = 1
	}

	private val userDatabase = UserDatabase.createInstance(this)

	private var lastRefresh: TextView? = null

	private var lastBackPress: Long = 0
	private var profileId: Long = -1
	private val items: ArrayList<WeekViewEvent<TimegridItem>> = ArrayList()
	private val loadedMonths = mutableListOf<Int>()
	private var displayedElement: PeriodElement? = null
	private var lastPickedDate: Calendar? = null
	private lateinit var profileUser: UserDatabase.User
	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var timetableLoader: TimetableLoader
	private lateinit var weekView: WeekView<TimegridItem>

	private var pbLoadingIndicator: ProgressBar? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		hasOwnToolbar = true

		super.onCreate(savedInstanceState)

		setupNotifications()

		if (!loadProfile()) {
			login()
			return
		}

		setContentView(R.layout.activity_main)

		setupActionBar()
		setupNavDrawer()

		setupViews()
		setupHours()

		setupTimetableLoader()
		showPersonalTimetable()
	}

	private fun login() {
		val loginIntent = Intent(this, LoginActivity::class.java)
		startActivity(loginIntent)
		// TODO: Start for result and set profileId from result
		finish()
	}

	private fun showPersonalTimetable() {
		val customType = TimetableDatabaseInterface.Type.valueOf(PreferenceUtils.getPrefString(
				preferences,
				"preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_TYPE}",
				TimetableDatabaseInterface.Type.SUBJECT.toString()
		))

		if (customType === TimetableDatabaseInterface.Type.SUBJECT) {
			profileUser.userData.elemType?.let { type ->
				setTarget(
						profileUser.userData.elemId,
						type,
						profileUser.userData.displayName)
			} ?: run {
				setTarget(anonymous = true)
			}
		} else {
			val customId = preferences.defaultPrefs.getInt("preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_ID}", -1)
			setTarget(customId, customType.toString(), timetableDatabaseInterface.getLongName(customId, customType))
		}
	}

	private fun setupNotifications() {
		val intent = Intent(this, StartupReceiver::class.java)
		sendBroadcast(intent)
	}

	private fun setupTimetableLoader() {
		timetableLoader = TimetableLoader(WeakReference(this), this, profileUser, timetableDatabaseInterface)
	}

	private fun setupNavDrawer() {
		val navigationView = findViewById<NavigationView>(R.id.navigationview_main)
		navigationView.setNavigationItemSelectedListener(this)
		navigationView.setCheckedItem(R.id.nav_show_personal)

		setupNavDrawerHeader(navigationView)

		val header = navigationView.getHeaderView(0)
		val dropdown = header.findViewById<ConstraintLayout>(R.id.constraintlayout_mainactivitydrawer_dropdown)
		val dropdownView = header.findViewById<LinearLayout>(R.id.linearlayout_mainactivitydrawer_dropdown_view)
		val dropdownImage = header.findViewById<ImageView>(R.id.imageview_mainactivitydrawer_dropdown_arrow)
		val dropdownList = header.findViewById<RecyclerView>(R.id.recyclerview_mainactivitydrawer_profile_list)

		profileListAdapter = ProfileListAdapter(this, userDatabase.getAllUsers().toMutableList(), View.OnClickListener { view ->
			toggleProfileDropdown(dropdownView, dropdownImage, dropdownList)
			switchToProfile(profileListAdapter.itemAt(dropdownList.getChildLayoutPosition(view)))
		}, View.OnLongClickListener { view ->
			deleteProfile(profileListAdapter.itemAt(dropdownList.getChildLayoutPosition(view)))
			true
		})
		dropdownList.adapter = profileListAdapter
		dropdown.setOnClickListener {
			toggleProfileDropdown(dropdownView, dropdownImage, dropdownList)
		}

		val profileListAdd = header.findViewById<LinearLayout>(R.id.linearlayout_mainactivitydrawer_add)
		profileListAdd.setOnClickListener {
			val loginIntent = Intent(this, LoginActivity::class.java)
			startActivity(loginIntent)
			// TODO: Start for result, set new profileId from result and reload
		}
	}

	private fun setupNavDrawerHeader(navigationView: NavigationView) {
		val line1 = if (profileUser.anonymous) getString(R.string.anonymous_name) else profileUser.userData.displayName
		val line2 = profileUser.userData.schoolName
		(navigationView.getHeaderView(0).findViewById<View>(R.id.textview_mainactivtydrawer_line1) as TextView).text =
				if (line1.isNullOrBlank()) getString(R.string.app_name) else line1
		(navigationView.getHeaderView(0).findViewById<View>(R.id.textview_mainactivitydrawer_line2) as TextView).text =
				if (line2.isBlank()) getString(R.string.all_contact_email) else line2
	}

	private fun toggleProfileDropdown(dropdownView: ViewGroup, dropdownImage: ImageView, dropdownList: RecyclerView) {
		if (dropdownImage.scaleY < 0) {
			dropdownImage.scaleY = 1F
			dropdownView.visibility = View.GONE
		} else {
			dropdownImage.scaleY = -1F

			dropdownList.setHasFixedSize(true)
			dropdownList.layoutManager = LinearLayoutManager(this)

			dropdownView.visibility = View.VISIBLE
		}
	}

	private fun deleteProfile(user: UserDatabase.User) {
		MaterialAlertDialogBuilder(this)
				.setTitle(getString(R.string.main_dialog_delete_profile_title))
				.setMessage(getString(R.string.main_dialog_delete_profile_message, user.userData.displayName, user.userData.schoolName))
				.setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.delete)) { _, _ ->
					userDatabase.deleteUser(user)
					if (user.id == profileId) {
						if (!loadProfile())
							login()

						recreate()
					}
					profileListAdapter.deleteUser(user)
					profileListAdapter.notifyDataSetChanged()
				}
				.show()
	}

	@SuppressLint("ApplySharedPref")
	private fun switchToProfile(user: UserDatabase.User) {
		preferences.saveProfileId(user.id!!)
		preferences.reload()
		if (!loadProfile()) finish() // TODO: Show error
		else {
			setupNavDrawerHeader(findViewById(R.id.navigationview_main))

			setupTimetableLoader()
			showPersonalTimetable()

			val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
			drawer.closeDrawer(GravityCompat.START)

			recreate()
		}
	}

	override fun onResume() {
		super.onResume()
		preferences.reload()
		setupWeekViewConfig()
		items.clear()
		loadedMonths.clear()
		weekView.invalidate()
	}

	private fun setupViews() {
		setupWeekView()

		pbLoadingIndicator = findViewById(R.id.progressbar_main_loading)

		lastRefresh = findViewById(R.id.textview_main_lastrefresh)
		lastRefresh?.text = getString(R.string.last_refreshed, getString(R.string.never))

		findViewById<Button>(R.id.button_main_settings).setOnClickListener {
			val intent = Intent(this@MainActivity, SettingsActivity::class.java)
			intent.putExtra(SettingsActivity.EXTRA_LONG_PROFILE_ID, profileId)
			// TODO: Find a way to jump directly to the personal timetable setting
			startActivity(intent)
		}

		setupSwipeRefresh()
	}

	private fun setupSwipeRefresh() {
		swiperefreshlayout_main_timetable.setOnRefreshListener {
			displayedElement?.let {
				Log.d("MainActivityDebug", "onRefresh called for months ${getDisplayedMonths()}")
				getDisplayedMonths().forEach { date ->
					loadTimetable(TimetableLoader.TimetableLoaderTarget(
							date.first,
							date.second,
							it.id, it.type),
							true)
				}
			}
		}
	}

	private fun getDisplayedMonths(): List<Pair<UntisDate, UntisDate>> {
		val displayedWeekStartDate = weekView.currentDate
		// TODO: Dynamic week length
		val displayedWeekEndDate = displayedWeekStartDate.plusDays(5)

		return if (displayedWeekStartDate.monthOfYear == displayedWeekEndDate.monthOfYear)
			listOf(Pair(
					UntisDate.fromLocalDate(displayedWeekStartDate.dayOfMonth().withMinimumValue()),
					UntisDate.fromLocalDate(displayedWeekStartDate.dayOfMonth().withMaximumValue())
			))
		else
			listOf(
					Pair(UntisDate.fromLocalDate(displayedWeekStartDate.dayOfMonth().withMinimumValue()),
							UntisDate.fromLocalDate(displayedWeekStartDate.dayOfMonth().withMaximumValue())),
					Pair(UntisDate.fromLocalDate(displayedWeekEndDate.dayOfMonth().withMinimumValue()),
							UntisDate.fromLocalDate(displayedWeekEndDate.dayOfMonth().withMaximumValue()))
			)
	}

	private fun loadTimetable(target: TimetableLoader.TimetableLoaderTarget, forceRefresh: Boolean = false) {
		Log.d("MainActivityDebug", "loadTimetable called for target $target")
		weekView.notifyDataSetChanged()
		showLoading(!forceRefresh)

		val alwaysLoad = PreferenceUtils.getPrefBool(preferences, "preference_timetable_refresh_in_background")
		val flags = (if (!forceRefresh) TimetableLoader.FLAG_LOAD_CACHE else 0) or (if (alwaysLoad || forceRefresh) TimetableLoader.FLAG_LOAD_SERVER else 0)
		timetableLoader.load(target, flags)
	}

	private fun loadProfile(): Boolean {
		if (userDatabase.getUsersCount() < 1)
			return false

		profileId = preferences.profileId
		if (profileId == 0L || userDatabase.getUser(profileId) == null) profileId = userDatabase.getAllUsers()[0].id
				?: 0 // Fall back to the first user if an invalid user id is saved
		if (profileId == 0L) return false // No user found in database
		profileUser = userDatabase.getUser(profileId) ?: return false

		preferences.saveProfileId(profileId)
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser.id ?: 0)
		return true
	}

	private fun setupWeekView() {
		weekView = findViewById(R.id.weekview_main_timetable)
		weekView.setOnEventClickListener(this)
		weekView.setOnCornerClickListener(this)
		weekView.setMonthChangeListener(this)
		setupWeekViewConfig()
	}

	private fun setupWeekViewConfig() {
		// Customization

		// Timetable
		weekView.columnGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_padding").toFloat(), this)
		weekView.overlappingEventGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_padding_overlap").toFloat(), this)
		weekView.eventCornerRadius = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_corner_radius").toFloat(), this)
		weekView.eventSecondaryTextCentered = PreferenceUtils.getPrefBool(preferences, "preference_timetable_centered_lesson_info")
		weekView.eventTextBold = PreferenceUtils.getPrefBool(preferences, "preference_timetable_bold_lesson_name")
		weekView.eventTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_lesson_name_font_size").toFloat(), this)
		weekView.eventSecondaryTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_lesson_info_font_size").toFloat(), this)
		weekView.eventTextColor = if (PreferenceUtils.getPrefBool(preferences, "preference_timetable_item_text_light")) Color.WHITE else Color.BLACK
		weekView.nowLineColor = PreferenceUtils.getPrefInt(preferences, "preference_marker")
	}

	override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<TimegridItem>> {
		val newYear = startDate.get(Calendar.YEAR)
		val newMonth = startDate.get(Calendar.MONTH)

		if (!loadedMonths.contains(newYear * 100 + newMonth)) {
			displayedElement?.let {
				loadedMonths.add(newYear * 100 + newMonth)
				loadTimetable(TimetableLoader.TimetableLoaderTarget(
						UntisDate.fromLocalDate(LocalDate(startDate)),
						UntisDate.fromLocalDate(LocalDate(endDate)),
						it.id, it.type))
			}
		}

		val matchedEvents = ArrayList<WeekViewDisplayable<TimegridItem>>()
		for (event in items) {
			if (eventMatches(event, newYear, newMonth)) {
				@Suppress("UNCHECKED_CAST")
				matchedEvents.add(event as WeekViewDisplayable<TimegridItem>)
			}
		}
		return matchedEvents
	}

	/**
	 * Checks if an event falls into a specific year and month.
	 * @param event The event to check for.
	 * @param year The year.
	 * @param month The month.
	 * @return True if the event matches the year and month.
	 */
	private fun eventMatches(event: WeekViewEvent<*>, year: Int, month: Int): Boolean {
		return event.startTime.get(Calendar.YEAR) == year
				&& event.startTime.get(Calendar.MONTH) == month
				|| event.endTime.get(Calendar.YEAR) == year
				&& event.endTime.get(Calendar.MONTH) == month
	}


	private fun setupHours() {
		val lines = MutableList(0) { return@MutableList 0 }

		// TODO: Replace the fixed day index AND/OR display a warning at login if the days are not equal AND/OR support inequal days
		profileUser.timeGrid.days[0].units.forEach { hour ->
			val startTime = DateTimeUtils.tTimeNoSeconds().parseLocalTime(hour.startTime).toString(DateTimeUtils.shortDisplayableTime())
			val endTime = DateTimeUtils.tTimeNoSeconds().parseLocalTime(hour.endTime).toString(DateTimeUtils.shortDisplayableTime())

			val startTimeParts = startTime.split(":")
			val endTimeParts = endTime.split(":")

			val startTimeInt = startTimeParts[0].toInt() * 60 + startTimeParts[1].toInt()
			val endTimeInt = endTimeParts[0].toInt() * 60 + endTimeParts[1].toInt()

			lines.add(startTimeInt)
			lines.add(endTimeInt)
		}

		weekView.hourLines = lines.toIntArray()

		weekView.startTime = lines[0]
		weekView.endTime = lines[lines.size - 1] + 30
	}

	private fun setupActionBar() {
		val toolbar: Toolbar = findViewById(R.id.toolbar_main)
		setSupportActionBar(toolbar)
		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		val toggle = ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open,
				R.string.navigation_drawer_close)
		drawer.addDrawerListener(toggle)
		toggle.syncState()
	}

	private fun prepareItems(items: List<TimegridItem>): List<TimegridItem> {
		// TODO: There may be a better way for this

		val timer = System.nanoTime()

		val days = profileUser.timeGrid.days

		val itemGrid: Array<Array<MutableList<TimegridItem>>> = Array(days.size) { Array(days[0].units.size) { mutableListOf<TimegridItem>() } }

		val newItems = mutableListOf<TimegridItem>()

		items.forEach { item ->
			val startDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.startDateTime)
			val endDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.endDateTime)

			val day = endDateTime.dayOfWeek - DateTimeConstants.MONDAY

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			item.periodData.durationInHours = thisUnitEndIndex + 1 - thisUnitStartIndex

			if (thisUnitStartIndex != -1)
				itemGrid[day][thisUnitStartIndex].add(item)
			else
				newItems.add(item)
		}

		itemGrid.forEach { units: Array<MutableList<TimegridItem>> ->
			units.forEachIndexed { unit, items ->
				if (unit == units.size - 1)
					return@forEachIndexed

				items.forEach { item ->
					var i = 1
					while (unit + i < units.size && item.mergeWith(units[unit + i]))
						i++
				}
			}
		}

		days.forEachIndexed { dayIndex, _ ->
			// TODO: Support holidays

			days[dayIndex].units.forEachIndexed { hourIndex, _ ->
				val allItems = itemGrid[dayIndex][hourIndex].toList()

				allItems.forEach { item ->
					//if (item.isHidden())
					//continue

					var rowSpan = item.periodData.durationInHours

					while (hourIndex + rowSpan < days.size) {
						val nextItems = itemGrid[dayIndex][hourIndex + rowSpan]
						if (item.mergeWith(nextItems)) {
							rowSpan++
							//timetable[0].addOffset(day, hour + rowSpan - 1);
						} else {
							break
						}
					}

					newItems.add(item)
				}
			}
		}

		setupColors(newItems)

		Log.d("prepareItems Timer", "prepareItems took ${(System.nanoTime() - timer) / 1000000.0}ms")
		return newItems
	}

	private fun setupColors(items: List<TimegridItem>) {
		val regularColor = PreferenceUtils.getPrefInt(preferences, "preference_background_regular")
		val examColor = PreferenceUtils.getPrefInt(preferences, "preference_background_exam")
		val cancelledColor = PreferenceUtils.getPrefInt(preferences, "preference_background_cancelled")
		val irregularColor = PreferenceUtils.getPrefInt(preferences, "preference_background_irregular")

		val regularPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_regular_past")
		val examPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_exam_past")
		val cancelledPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_cancelled_past")
		val irregularPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_irregular_past")

		val useDefault = PreferenceUtils.getPrefBool(preferences, "preference_use_default_background")
		val useTheme = PreferenceUtils.getPrefBool(preferences, "preference_use_theme_background")

		items.forEach { item ->
			item.color = when {
				item.periodData.isExam() -> examColor
				item.periodData.isCancelled() -> cancelledColor
				item.periodData.isIrregular() -> irregularColor
				useDefault -> Color.parseColor(item.periodData.element.backColor)
				useTheme -> getAttr(R.attr.colorPrimary)
				else -> regularColor

			}

			item.pastColor = when {
				item.periodData.isExam() -> examPastColor
				item.periodData.isCancelled() -> cancelledPastColor
				item.periodData.isIrregular() -> irregularPastColor
				useDefault -> item.color.darken(0.25f)
				useTheme -> getAttr(R.attr.colorPrimaryDark)
				else -> regularPastColor
			}
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		/*when (displayedElement.getElemType()) {
			"CLASS" -> (findViewById<View>(R.id.nav_view) as NavigationView)
					.setCheckedItem(R.id.nav_show_classes)
			"TEACHER" -> (findViewById<View>(R.id.nav_view) as NavigationView)
					.setCheckedItem(R.id.nav_show_teachers)
			"ROOM" -> (findViewById<View>(R.id.nav_view) as NavigationView)
					.setCheckedItem(R.id.nav_show_rooms)
			else -> (findViewById<View>(R.id.nav_view) as NavigationView)
					.setCheckedItem(R.id.nav_show_personal)
		}*/

		when (item.itemId) {
			R.id.nav_show_personal -> {
				showPersonalTimetable()
			}
			R.id.nav_show_classes -> {
				showItemList(TimetableDatabaseInterface.Type.CLASS)
			}
			R.id.nav_show_teachers -> {
				showItemList(TimetableDatabaseInterface.Type.TEACHER)
			}
			R.id.nav_show_rooms -> {
				showItemList(TimetableDatabaseInterface.Type.ROOM)
			}
			R.id.nav_settings -> {
				val intent = Intent(this@MainActivity, SettingsActivity::class.java)
				intent.putExtra(SettingsActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivity(intent)
			}
			R.id.nav_free_rooms -> {
				val i3 = Intent(this@MainActivity, RoomFinderActivity::class.java)
				i3.putExtra(RoomFinderActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivityForResult(i3, REQUEST_CODE_ROOM_FINDER)
			}
			R.id.nav_donations -> {
				/*val i4 = Intent(this@ActivityMain, ActivityDonations::class.java)
				startActivity(i4)*/
			}
			R.id.nav_share -> {
				/*Answers.newInstance().logShare(ShareEvent()
						.putMethod("Share via Intent")
						.putContentName("Share the BetterUntis download link")
						.putContentType("share")
						.putContentId(CONTENT_ID_SHARE))

				val i = Intent(Intent.ACTION_SEND)
				i.type = "text/plain"
				i.putExtra(Intent.EXTRA_SUBJECT, getFirebaseString("recommendation_subject"))
				i.putExtra(Intent.EXTRA_TEXT, getFirebaseString("recommendation_text"))
				startActivity(Intent.createChooser(i, getString(R.string.link_sending_caption, getString(R.string.app_name))))*/
			}
		}

		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		drawer.closeDrawer(GravityCompat.START)
		return true
	}

	private fun showItemList(type: TimetableDatabaseInterface.Type) {
		ElementPickerDialog.newInstance(
				timetableDatabaseInterface,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(type)
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)

		when (requestCode) {
			REQUEST_CODE_ROOM_FINDER -> {
				if (resultCode == Activity.RESULT_OK) {
					val roomId = data?.getIntExtra(RoomFinderActivity.EXTRA_INT_ROOM_ID, -1) ?: -1
					if (roomId != -1)
						setTarget(roomId, TimetableDatabaseInterface.Type.ROOM.toString(), timetableDatabaseInterface.getLongName(roomId, TimetableDatabaseInterface.Type.ROOM))
				}
			}
		}
	}

	override fun onBackPressed() {
		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START)
		} else {
			if (displayedElement?.id != profileUser.userData.elemId) {
				// Go to personal timetable
				profileUser.userData.elemType?.let { type ->
					setTarget(
							profileUser.userData.elemId,
							type,
							profileUser.userData.displayName)
				} ?: run {
					setTarget(anonymous = true)
				}
			} else {
				if (System.currentTimeMillis() - 2000 > lastBackPress) {
					Snackbar.make(findViewById<ConstraintLayout>(R.id.content_main),
							R.string.snackbar_press_back_double, 2000).show()
					lastBackPress = System.currentTimeMillis()
				} else {
					super.onBackPressed()
				}
			}
		}
	}

	private fun setTarget(anonymous: Boolean) {
		if (anonymous) {
			showLoading(false)

			displayedElement = null

			loadedMonths.clear()
			items.clear()
			weekView.notifyDataSetChanged()

			supportActionBar?.title = getString(R.string.anonymous_name)
			constraintlayout_main_anonymouslogininfo.visibility = View.VISIBLE
		} else {
			constraintlayout_main_anonymouslogininfo.visibility = View.GONE
		}
	}

	private fun setTarget(id: Int, type: String, displayName: String?) {
		setTarget(anonymous = false)

		displayedElement = PeriodElement(type, id, id)
		loadedMonths.clear()
		items.clear()
		weekView.notifyDataSetChanged()
		supportActionBar?.title = displayName ?: getString(R.string.app_name)
	}

	override fun onEventClick(data: TimegridItem?, eventRect: RectF?) {
		data?.let { item ->
			showLessonInfo(item)
		}
	}

	override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?, useOrgId: Boolean) {
		dialog.dismiss()
		element?.let {
			setTarget(if (useOrgId) element.orgId else element.id, element.type, timetableDatabaseInterface.getLongName(
					if (useOrgId) element.orgId else element.id, TimetableDatabaseInterface.Type.valueOf(element.type)))
		} ?: run {
			showPersonalTimetable()
		}
		refreshNavigationViewSelection()
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {
		refreshNavigationViewSelection()
	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		dialog.dismiss() // unused, but just in case
	}

	private fun refreshNavigationViewSelection() {
		when (displayedElement?.type) {
			"CLASS" -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_classes)
			"TEACHER" -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_teachers)
			"ROOM" -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_rooms)
			else -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_personal)
		}
	}

	private fun showLessonInfo(item: TimegridItem) {
		TimetableItemDetailsDialog.createInstance(item, timetableDatabaseInterface).show(supportFragmentManager, "itemDetails") // TODO: Remove hard-coded tag
	}

	// TODO: Implement this properly and re-enable the view in XML
	private fun setLastRefresh(timestamp: Long) {
		if (timestamp == -1L)
			lastRefresh?.text = getString(R.string.last_refreshed, getString(R.string.never))
		else
			lastRefresh?.text = getString(R.string.last_refreshed, formatTimeDiff(Instant.now().millis - timestamp))
	}

	private fun formatTimeDiff(diff: Long): String {
		// TODO: Great candidate for unit tests
		return when {
			diff < MINUTE_MILLIS -> getString(R.string.time_diff_just_now)
			diff < HOUR_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_minutes, ((diff / MINUTE_MILLIS).toInt()), diff / MINUTE_MILLIS)
			diff < DAY_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_hours, ((diff / HOUR_MILLIS).toInt()), diff / HOUR_MILLIS)
			else -> resources.getQuantityString(R.plurals.main_time_diff_days, ((diff / DAY_MILLIS).toInt()), diff / DAY_MILLIS)
		}
	}

	override fun addData(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
		Log.d("MainActivityDebug", "addData received ${items.size} items from $startDate until $endDate")
		this.items.removeAll(this.items.filter {
			// TODO: Look at the timetable htl-salzburg:4CHEL between February and March: Fix multi-day lessons over multiple months disappearing when refreshing
			LocalDateTime(it.startTime).isBetween(startDate.toLocalDateTime(), endDate.toLocalDateTime())
					|| LocalDateTime(it.endTime).isBetween(startDate.toLocalDateTime(), endDate.toLocalDateTime())
		})

		val preparedItems = prepareItems(items)
		this.items.addAll(preparedItems)
		weekView.notifyDataSetChanged()

		// TODO: Only disable these loading indicators when everything finished loading
		swiperefreshlayout_main_timetable.isRefreshing = false
		showLoading(false)

		setLastRefresh(timestamp)
	}

	override fun onError(requestId: Int, code: Int?, message: String?) {
		when (code) {
			TimetableLoader.CODE_CACHE_MISSING -> timetableLoader.repeat(requestId, TimetableLoader.FLAG_LOAD_SERVER)
			else -> {
				showLoading(false)
				Snackbar.make(content_main, if (code != null) ErrorMessageDictionary.getErrorMessage(resources, code) else message
						?: getString(R.string.error), Snackbar.LENGTH_INDEFINITE)
						.setAction("Show") { showErrorDialog(requestId, code, message) }
						.show()
			}
		}
	}

	private fun showErrorDialog(requestId: Int, code: Int?, message: String?) {
		val dialog = AlertDialog.Builder(this)
				.setTitle("Error Information")
				.setMessage("Request ID: $requestId\nError code: $code\nError message: $message")
				.setPositiveButton(R.string.ok) { dialog, _ ->
					dialog.dismiss()
				}
				.create()

		// TODO: Add button to create a GitHub issue
		dialog.show()
	}

	private fun showLoading(loading: Boolean) {
		pbLoadingIndicator?.visibility = if (loading) View.VISIBLE else View.GONE
	}

	override fun onCornerClick() {
		val fragment = DatePickerDialog()

		lastPickedDate?.let {
			val args = Bundle()
			args.putInt("year", it.get(Calendar.YEAR))
			args.putInt("month", it.get(Calendar.MONTH))
			args.putInt("day", it.get(Calendar.DAY_OF_MONTH))
			fragment.arguments = args
		}
		fragment.setOnDateSetListener(android.app.DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
			val c = DateTimeUtils.today()
			c.set(Calendar.YEAR, year)
			c.set(Calendar.MONTH, month)
			c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
			weekView.goToDate(c)

			lastPickedDate = c
		})
		fragment.show(
				supportFragmentManager, "datePicker")
	}

	private fun Int.darken(ratio: Float): Int {
		return ColorUtils.blendARGB(this, Color.BLACK, ratio)
	}

	private fun <T> Comparable<T>.isBetween(start: T, end: T): Boolean {
		return this >= start && this <= end
	}
}
