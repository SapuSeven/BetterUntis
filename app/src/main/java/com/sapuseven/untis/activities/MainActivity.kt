package com.sapuseven.untis.activities

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import com.alamkanak.weekview.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.User
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.dialogs.TimetableItemDetailsDialog
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.KotlinUtils
import com.sapuseven.untis.helpers.KotlinUtils.safeLet
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
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
		ElementPickerDialog.ElementPickerDialogListener,
		DatePickerDialog.OnDateSetListener {

	companion object {
		private const val MINUTE_MILLIS: Int = 60 * 1000
		private const val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
		private const val DAY_MILLIS: Int = 24 * HOUR_MILLIS
	}

	//private var listManager: ListManager? = null
	//private var pagerHeaderAdapter: TimetableHeaderAdapter? = null
	//private var pagerTableAdapter: TimetableAdapter? = null
	//private var dialog: AlertDialog? = null
	//private var profile: JSONObject? = null
	//private var displayedElement: SessionInfo? = null
	//private var userData: UserData? = null

	private val userDatabase = UserDatabase.createInstance(this) // TODO: Ensure that only one instance of userDatabase exists and that there is no simultaneous access between threads

	private var lastRefresh: TextView? = null

	private var lastBackPress: Long = 0
	private var profileId: Long = -1
	private var profileUser: User? = null
	private var preferenceManager: PreferenceManager? = null
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null
	private val items: ArrayList<WeekViewEvent<TimegridItem>> = ArrayList()
	private val loadedMonths = mutableListOf<Int>()
	private var displayedElement: PeriodElement? = null

	private var weekView: WeekView<TimegridItem>? = null
	private var pbLoadingIndicator: ProgressBar? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		hasOwnToolbar = true

		super.onCreate(savedInstanceState)

		preferenceManager = PreferenceManager(this)

		if (loadProfile()) {
			val loginIntent = Intent(this, LoginActivity::class.java)
			startActivity(loginIntent)
			finish()
			return
		}

		setContentView(R.layout.activity_main)

		setupActionBar()

		/*val localDate = LocalDate()
		if (setOf(SATURDAY, SUNDAY).contains(localDate.dayOfWeek) && localDate.dayOfWeek().withMinimumValue().dayOfWeek == MONDAY)
			currentViewPos++*/

		/*val ivSelectDate = findViewById<View>(R.id.ivSelectDate)
		ivSelectDate.setOnClickListener({ view ->
			val fragment = DatePickerFragment()
			val args = Bundle()
			args.putInt("year", lastCalendar.get(Calendar.YEAR))
			args.putInt("month", lastCalendar.get(Calendar.MONTH))
			args.putInt("day", lastCalendar.get(Calendar.DAY_OF_MONTH))
			fragment.arguments = args
			fragment.show(supportFragmentManager, "datePicker")
		})*/

		/*val alternatingHours = defaultPrefs.getBoolean("preference_alternating_hours", false)

		var alternativeBackgroundColor = resources.getInteger(R.integer.preference_alternating_color_default_light)
		if (getPrefBool(this, defaultPrefs, "preference_alternating_colors_use_custom"))
			alternativeBackgroundColor = getPrefInt(this, defaultPrefs, "preference_alternating_color")
		else if (defaultPrefs.getBoolean("preference_dark_theme", false))
			alternativeBackgroundColor = resources.getInteger(R.integer.preference_alternating_color_default_dark)*/

		val navigationView = findViewById<NavigationView>(R.id.navigationview_main)
		navigationView.setNavigationItemSelectedListener(this)
		navigationView.setCheckedItem(R.id.nav_show_personal)

		val line1 = profileUser?.userData?.displayName
		val line2 = profileUser?.userData?.schoolName
		(navigationView.getHeaderView(0).findViewById<View>(R.id.textview_activitymaindrawer_line1) as TextView).text =
				if (line1.isNullOrBlank()) getString(R.string.app_name) else line1
		(navigationView.getHeaderView(0).findViewById<View>(R.id.textview_activitymaindrawer_line2) as TextView).text =
				if (line2.isNullOrBlank()) getString(R.string.all_contact_email) else line2

		setupViews()
		setupHours()

		profileUser?.let { user ->
			setTarget(user.userData.elemId, user.userData.elemType, user.userData.displayName)
		}
	}

	override fun onResume() {
		super.onResume()
		setupWeekViewConfig()
		items.clear()
		weekView?.invalidate()
	}

	private fun setupViews() {
		setupWeekView()

		pbLoadingIndicator = findViewById(R.id.progressbar_main_loading)

		lastRefresh = findViewById(R.id.textview_main_lastrefresh)
		lastRefresh?.text = getString(R.string.last_refreshed, getString(R.string.never))
	}

	private fun loadTimetable(startDate: UntisDate, endDate: UntisDate, id: Int, type: String) {
		weekView?.notifyDataSetChanged()
		showLoading(true)

		KotlinUtils.safeLet(profileUser, timetableDatabaseInterface) { user, db ->
			TimetableLoader(WeakReference(this), this, user, db).load(startDate, endDate, id, type)
		}
	}

	private fun loadProfile(): Boolean {
		//userDatabase.onUpgrade(userDatabase.writableDatabase, 0, 0) // TODO: This deletes all saved profiles, remove after testing

		if (userDatabase.getUsersCount() < 1)
			return true

		preferenceManager?.let { profileId = it.defaultPrefs.getInt("profile", -1).toLong() }  // TODO: Do not hard-code "profile"
		profileId = userDatabase.getAllUsers()[0].id
				?: 0 // TODO: Debugging only. This is a dynamic id.
		profileUser = userDatabase.getUser(profileId) // TODO: Show error (invalid profile) if (profileId == -1) or (profileUser == null) and default to the first profile/re-login if necessary. It is mandatory to stop the execution of more code, or else the app will crash.

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser?.id ?: -1)
		return false
	}

	private fun setupWeekView() {
		weekView = findViewById(R.id.weekView)
		weekView?.setOnEventClickListener(this)
		weekView?.setOnCornerClickListener(this)
		weekView?.setMonthChangeListener(this)
		setupWeekViewConfig()
	}

	private fun setupWeekViewConfig() {
		safeLet(weekView, preferenceManager) { weekView, preferenceManager ->
			// Customization

			// Timetable
			weekView.columnGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_item_padding").toFloat(), this)
			weekView.overlappingEventGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_item_padding_overlap").toFloat(), this)
			weekView.eventCornerRadius = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_item_corner_radius").toFloat(), this)
			weekView.eventSecondaryTextCentered = PreferenceUtils.getPrefBool(preferenceManager, "preference_timetable_centered_lesson_info")
			weekView.eventTextBold = PreferenceUtils.getPrefBool(preferenceManager, "preference_timetable_bold_lesson_name")
			weekView.eventTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_lesson_name_font_size").toFloat(), this)
			weekView.eventSecondaryTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_lesson_info_font_size").toFloat(), this)
			weekView.eventTextColor = if (PreferenceUtils.getPrefBool(preferenceManager, "preference_timetable_item_text_light")) Color.WHITE else Color.BLACK
			weekView.nowLineColor = PreferenceUtils.getPrefInt(preferenceManager, "preference_marker")
		}
	}

	override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<TimegridItem>> {
		val newYear = startDate.get(Calendar.YEAR)
		val newMonth = startDate.get(Calendar.MONTH)

		if (!loadedMonths.contains(newMonth)) {
			displayedElement?.let {
				loadedMonths.add(newMonth)
				loadTimetable(
						UntisDate(LocalDate(startDate).toString(ISODateTimeFormat.date())),
						UntisDate(LocalDate(endDate).toString(ISODateTimeFormat.date())),
						it.id, it.type)
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
		// TODO: Prevent from crashing
		profileUser!!.timeGrid.days[0].units.forEach { hour ->
			val startTime = DateTimeUtils.tTimeNoSeconds().parseLocalTime(hour.startTime).toString(DateTimeUtils.shortDisplayableTime())
			val endTime = DateTimeUtils.tTimeNoSeconds().parseLocalTime(hour.endTime).toString(DateTimeUtils.shortDisplayableTime())

			val startTimeParts = startTime.split(":")
			val endTimeParts = endTime.split(":")

			val startTimeInt = startTimeParts[0].toInt() * 60 + startTimeParts[1].toInt()
			val endTimeInt = endTimeParts[0].toInt() * 60 + endTimeParts[1].toInt()

			lines.add(startTimeInt)
			lines.add(endTimeInt)
		}

		weekView?.hourLines = lines.toIntArray()

		weekView?.startTime = lines[0]
		weekView?.endTime = lines[lines.size - 1] + 30
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
		// TODO: Prevent from crashing, inspect whole method !! usages
		// TODO: There may be a better way for this

		val timer = System.nanoTime()

		val days = profileUser!!.timeGrid.days

		val itemGrid: Array<Array<MutableList<TimegridItem>>> = Array(days.size) { Array(days[0].units.size) { mutableListOf<TimegridItem>() } }

		val newItems = mutableListOf<TimegridItem>()

		items.forEach { item ->
			val startDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.startDateTime)
			val endDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.endDateTime)

			val day = endDateTime.dayOfWeek - DateTimeConstants.MONDAY


			// TODO: Crashes at teacher:KLAF@htl-salzburg (day out of bounds)
			// TODO: This can probably be optimized
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
		preferenceManager?.let { prefs ->
			val regularColor = PreferenceUtils.getPrefInt(prefs, "preference_background_regular")
			val examColor = PreferenceUtils.getPrefInt(prefs, "preference_background_exam")
			val cancelledColor = PreferenceUtils.getPrefInt(prefs, "preference_background_cancelled")
			val irregularColor = PreferenceUtils.getPrefInt(prefs, "preference_background_irregular")

			val regularPastColor = PreferenceUtils.getPrefInt(prefs, "preference_background_regular_past")
			val examPastColor = PreferenceUtils.getPrefInt(prefs, "preference_background_exam_past")
			val cancelledPastColor = PreferenceUtils.getPrefInt(prefs, "preference_background_cancelled_past")
			val irregularPastColor = PreferenceUtils.getPrefInt(prefs, "preference_background_irregular_past")

			val useDefault = PreferenceUtils.getPrefBool(prefs, "preference_use_default_background")
			val useTheme = PreferenceUtils.getPrefBool(prefs, "preference_use_theme_background")

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
				/*val customType = fromValue(PreferenceUtils.getPrefInt(preferenceManager, "preference_timetable_personal_timetable", UNKNOWN.value))

				if (customType === UNKNOWN) {*/
				profileUser?.let { user ->
					setTarget(
							user.userData.elemId,
							user.userData.elemType,
							user.userData.displayName) // TODO: Display name should be anonymous, check if it works that way
				}
				/*} else {
					val customId = prefs.getInt("preference_timetable_personal_timetable_id", -1)
					val customName = prefs.getString("preference_timetable_personal_timetable_name", "")
					setTarget(customId, customType, customName)
				}*/
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
				startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
			}
			R.id.nav_free_rooms -> {
				/*val i3 = Intent(this@ActivityMain, ActivityRoomFinder::class.java)
				startActivityForResult(i3, REQUEST_CODE_ROOM_FINDER)*/
			}
			R.id.nav_donations -> {
				/*val i4 = Intent(this@ActivityMain, ActivityDonations::class.java)
				startActivity(i4)*/
			}
			R.id.nav_share -> {
				/*Answers.createInstance().logShare(ShareEvent()
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
		timetableDatabaseInterface?.let { timetableDatabaseInterface ->
			ElementPickerDialog.createInstance(timetableDatabaseInterface, type).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code
		}
	}

	override fun onBackPressed() {
		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START)
		} else {
			if (displayedElement?.id != profileUser?.userData?.elemId) {
				// Go to personal timetable
				profileUser?.let { user ->
					setTarget(
							user.userData.elemId,
							user.userData.elemType,
							user.userData.displayName)
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

	private fun setTarget(id: Int, type: String, displayName: String) {
		displayedElement = PeriodElement(type, id)
		loadedMonths.clear()
		items.clear()
		weekView?.notifyDataSetChanged()
		supportActionBar?.title = displayName
	}

	override fun onEventClick(data: TimegridItem?, eventRect: RectF?) {
		data?.let { item ->
			showLessonInfo(item)
		}
	}

	override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?) {
		if (element == null) {
			profileUser?.let { user ->
				setTarget(
						user.userData.elemId,
						user.userData.elemType,
						user.userData.displayName) // TODO: This should be anonymous, check if it works that way
			}
		}
		element?.let {
			dialog.dismiss()
			setTarget(element.id, element.type, timetableDatabaseInterface?.getLongName(
					element.id, TimetableDatabaseInterface.Type.valueOf(element.type))
					?: getString(R.string.app_name))
			refreshNavigationViewSelection()
		}
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {
		refreshNavigationViewSelection()
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

	private fun setLastRefresh(timestamp: Long) {
		if (timestamp == -1L)
			lastRefresh?.text = getString(R.string.last_refreshed, getString(R.string.never))
		else
			lastRefresh?.text = getString(R.string.last_refreshed, formatTimeDiff(Instant.now().millis - timestamp))
	}

	private fun formatTimeDiff(diff: Long): String {
		return when {
			diff < MINUTE_MILLIS -> getString(R.string.time_diff_just_now)
			diff < 50 * MINUTE_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_minutes, ((diff / MINUTE_MILLIS).toInt()), diff / MINUTE_MILLIS)
			diff < 24 * HOUR_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_hours, ((diff / HOUR_MILLIS).toInt()), diff / HOUR_MILLIS)
			else -> resources.getQuantityString(R.plurals.main_time_diff_days, ((diff / DAY_MILLIS).toInt()), diff / DAY_MILLIS)
		}
	}

	override fun addData(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
		this.items.removeAll(this.items.filter {
			// TODO: Look at the timetable htl-salzburg:4CHEL between February and March: Fix multi-day lessons over multiple months disappearing when refreshing
			LocalDateTime(it.startTime) >= startDate.toLocalDateTime() && LocalDateTime(it.startTime) <= endDate.toLocalDateTime().plusDays(1)
					|| LocalDateTime(it.endTime) >= startDate.toLocalDateTime() && LocalDateTime(it.endTime) <= endDate.toLocalDateTime().plusDays(1)
		})

		this.items.addAll(prepareItems(items))
		weekView?.notifyDataSetChanged()
		showLoading(false)
		setLastRefresh(timestamp)
	}

	private fun showLoading(loading: Boolean) {
		pbLoadingIndicator?.visibility = if (loading) View.VISIBLE else View.GONE
	}

	override fun onCornerClick() {
		val c = Calendar.getInstance()
		val year = c.get(Calendar.YEAR)
		val month = c.get(Calendar.MONTH)
		val day = c.get(Calendar.DAY_OF_MONTH)
		val datePickerDialog = DatePickerDialog(this, android.R.style.Theme_Material_Dialog, this, year, month, day)
		datePickerDialog.show()

		/*val fragment = DatePickerFragment()
		val args = Bundle()
		args.putInt(DIALOG_THEME, R.style.Dialog)
		fragment.arguments = args
		fragment.callback = {
			weekView?.goToDate(it)
		}
		fragment.show(supportFragmentManager, "datePicker")*/
	}


	override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.YEAR, year)
		calendar.set(Calendar.MONTH, month)
		calendar.set(Calendar.DAY_OF_MONTH, day)
		weekView?.goToDate(calendar)
	}

	private fun Int.darken(ratio: Float): Int {
		return ColorUtils.blendARGB(this, Color.BLACK, ratio)
	}
}
