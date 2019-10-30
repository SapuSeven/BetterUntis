package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.HolidayChip
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.listeners.EventClickListener
import com.alamkanak.weekview.listeners.ScrollListener
import com.alamkanak.weekview.listeners.TopLeftCornerClickListener
import com.alamkanak.weekview.loaders.WeekViewLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.dialogs.DatePickerDialog
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.dialogs.ErrorReportingDialog
import com.sapuseven.untis.dialogs.TimetableItemDetailsDialog
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.Holiday
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.notifications.NotificationSetup.Companion.EXTRA_BOOLEAN_MANUAL
import com.sapuseven.untis.notifications.StartupReceiver
import com.sapuseven.untis.preferences.ElementPickerPreference
import kotlinx.android.synthetic.main.activity_main_content.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.lang.ref.WeakReference

class MainActivity :
		BaseActivity(),
		NavigationView.OnNavigationItemSelectedListener,
		WeekViewLoader.PeriodChangeListener<TimegridItem>,
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
		private const val REQUEST_CODE_SETTINGS = 2
		private const val REQUEST_CODE_LOGINDATAINPUT_ADD = 3
		private const val REQUEST_CODE_LOGINDATAINPUT_EDIT = 4
	}

	private val userDatabase = UserDatabase.createInstance(this)
	private var lastBackPress: Long = 0
	private var profileId: Long = -1
	private val weeklyTimetableItems: MutableMap<Int, WeeklyTimetableItems?> = mutableMapOf()
	private var displayedElement: PeriodElement? = null
	private var lastPickedDate: DateTime? = null
	private var proxyHost: String? = null
	private var profileUpdateDialog: AlertDialog? = null
	private var currentWeekIndex = 0
	private lateinit var profileUser: UserDatabase.User
	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var timetableLoader: TimetableLoader
	private lateinit var weekView: WeekView<TimegridItem>

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
		setupHolidays()

		if (profileUser.schoolId <= 0) return

		setupTimetableLoader()
		showPersonalTimetable()
		refreshNavigationViewSelection()
	}

	override fun onResume() {
		super.onResume()
		preferences.reload()
		proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		setupWeekViewConfig()
		weekView.invalidate()

		if (profileUser.schoolId <= 0 && profileUpdateDialog == null)
			showProfileUpdateRequired()
	}

	private fun showProfileUpdateRequired() {
		profileUpdateDialog = MaterialAlertDialogBuilder(this)
				.setTitle(getString(R.string.main_dialog_update_profile_title))
				.setMessage(getString(R.string.main_dialog_update_profile_message))
				.setPositiveButton(getString(R.string.main_dialog_update_profile_button)) { _, _ ->
					editProfile(profileUser)
				}
				.setCancelable(false)
				.show()
	}

	private fun login() {
		val loginIntent = Intent(this, LoginActivity::class.java)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_ADD)
		finish()
	}

	private fun showPersonalTimetable(): Boolean {
		@Suppress("RemoveRedundantQualifierName")
		val customType = TimetableDatabaseInterface.Type.valueOf(PreferenceUtils.getPrefString(
				preferences,
				"preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_TYPE}",
				TimetableDatabaseInterface.Type.SUBJECT.toString()
		))

		if (customType === TimetableDatabaseInterface.Type.SUBJECT) {
			profileUser.userData.elemType?.let { type ->
				return setTarget(
						profileUser.userData.elemId,
						type,
						profileUser.userData.displayName)
			} ?: run {
				return setTarget(anonymous = true)
			}
		} else {
			val customId = preferences.defaultPrefs.getInt("preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_ID}", -1)
			return setTarget(customId, customType.toString(), timetableDatabaseInterface.getLongName(customId, customType))
		}
	}

	private fun setupNotifications() {
		val intent = Intent(this, StartupReceiver::class.java)
		intent.putExtra(EXTRA_BOOLEAN_MANUAL, true)
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
			closeDrawer()
			editProfile(profileListAdapter.itemAt(dropdownList.getChildLayoutPosition(view)))
			true
		})
		dropdownList.adapter = profileListAdapter
		dropdown.setOnClickListener {
			toggleProfileDropdown(dropdownView, dropdownImage, dropdownList)
		}

		val profileListAdd = header.findViewById<LinearLayout>(R.id.linearlayout_mainactivitydrawer_add)
		profileListAdd.setOnClickListener {
			closeDrawer()
			addProfile()
		}
	}

	private fun setupNavDrawerHeader(navigationView: NavigationView) {
		val line1 = if (profileUser.anonymous) getString(R.string.all_anonymous) else profileUser.userData.displayName
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

	private fun addProfile() {
		val loginIntent = Intent(this, LoginActivity::class.java)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_ADD)
	}

	private fun editProfile(user: UserDatabase.User) {
		val loginIntent = Intent(this, LoginDataInputActivity::class.java)
		loginIntent.putExtra(LoginDataInputActivity.EXTRA_LONG_PROFILE_ID, user.id)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_EDIT)
	}

	@SuppressLint("ApplySharedPref")
	private fun switchToProfile(user: UserDatabase.User) {
		preferences.saveProfileId(user.id!!)
		preferences.reload()
		if (!loadProfile()) finish() // TODO: Show error
		else {
			setupNavDrawerHeader(findViewById(R.id.navigationview_main))

			closeDrawer()
			setupTimetableLoader()
			showPersonalTimetable()
			refreshNavigationViewSelection()

			recreate()
		}
	}

	private fun setupViews() {
		setupWeekView()

		textview_main_lastrefresh?.text = getString(R.string.main_last_refreshed, getString(R.string.main_last_refreshed_never))

		findViewById<Button>(R.id.button_main_settings).setOnClickListener {
			val intent = Intent(this@MainActivity, SettingsActivity::class.java)
			intent.putExtra(SettingsActivity.EXTRA_LONG_PROFILE_ID, profileId)
			// TODO: Find a way to jump directly to the personal timetable setting
			startActivityForResult(intent, REQUEST_CODE_SETTINGS)
		}

		setupSwipeRefresh()
	}

	private fun setupSwipeRefresh() {
		swiperefreshlayout_main_timetable.setOnRefreshListener {
			displayedElement?.let { element ->
				weeklyTimetableItems[currentWeekIndex]?.dateRange?.let { dateRange ->
					loadTimetable(TimetableLoader.TimetableLoaderTarget(dateRange.first, dateRange.second, element.id, element.type), true)
				}
			}
		}
	}

	private fun loadTimetable(target: TimetableLoader.TimetableLoaderTarget, forceRefresh: Boolean = false) {
		weekView.notifyDataSetChanged()
		if (!forceRefresh) showLoading(true)

		val alwaysLoad = PreferenceUtils.getPrefBool(preferences, "preference_connectivity_refresh_in_background")
		val flags = (if (!forceRefresh) TimetableLoader.FLAG_LOAD_CACHE else 0) or (if (alwaysLoad || forceRefresh) TimetableLoader.FLAG_LOAD_SERVER else 0)
		timetableLoader.load(target, flags, proxyHost)
	}

	private fun loadProfile(): Boolean {
		if (userDatabase.getUsersCount() < 1)
			return false

		profileId = preferences.currentProfileId()
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
		weekView.setPeriodChangeListener(this)
		weekView.scrollListener = object : ScrollListener {
			override fun onFirstVisibleDayChanged(newFirstVisibleDay: DateTime, oldFirstVisibleDay: DateTime?) {
				currentWeekIndex = convertDateTimeToWeekIndex(newFirstVisibleDay)
				setLastRefresh(weeklyTimetableItems[currentWeekIndex]?.lastUpdated
						?: 0)
			}
		}
		setupWeekViewConfig()
	}

	private fun setupWeekViewConfig() {
		weekView.numberOfVisibleDays = profileUser.timeGrid.days.size

		// Customization

		// Timetable
		weekView.columnGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_padding").toFloat(), this).toInt()
		weekView.overlappingEventGap = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_padding_overlap").toFloat(), this).toInt()
		weekView.eventCornerRadius = ConversionUtils.dpToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_item_corner_radius").toFloat(), this).toInt()
		weekView.eventSecondaryTextCentered = PreferenceUtils.getPrefBool(preferences, "preference_timetable_centered_lesson_info")
		weekView.eventTextBold = PreferenceUtils.getPrefBool(preferences, "preference_timetable_bold_lesson_name")
		weekView.eventTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_lesson_name_font_size").toFloat(), this)
		weekView.eventSecondaryTextSize = ConversionUtils.spToPx(PreferenceUtils.getPrefInt(preferences, "preference_timetable_lesson_info_font_size").toFloat(), this)
		weekView.eventTextColor = if (PreferenceUtils.getPrefBool(preferences, "preference_timetable_item_text_light")) Color.WHITE else Color.BLACK
		weekView.nowLineColor = PreferenceUtils.getPrefInt(preferences, "preference_marker")
	}

	override fun onPeriodChange(startDate: DateTime, endDate: DateTime): List<WeekViewDisplayable<TimegridItem>> {
		val weekIndex = convertDateTimeToWeekIndex(startDate)
		return weeklyTimetableItems[weekIndex]?.items ?: run {
			displayedElement?.let { displayedElement ->
				weeklyTimetableItems[weekIndex] = WeeklyTimetableItems().apply {
					dateRange = (UntisDate.fromLocalDate(LocalDate(startDate)) to UntisDate.fromLocalDate(LocalDate(endDate))).also { dateRange ->
						loadTimetable(TimetableLoader.TimetableLoaderTarget(dateRange.first, dateRange.second, displayedElement.id, displayedElement.type))
					}
				}
			}
			emptyList<WeekViewDisplayable<TimegridItem>>()
		}
	}

	private fun convertDateTimeToWeekIndex(date: DateTime) = date.year * 100 + date.dayOfYear / 7

	private fun setupHours() {
		val lines = MutableList(0) { return@MutableList 0 }

		profileUser.timeGrid.days.maxBy { it.units.size }?.units?.forEach { hour ->
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
		weekView.endTime = lines[lines.size - 1] + 30 // TODO: Don't hard-code this offset
	}

	private fun setupHolidays() {
		userDatabase.getAdditionalUserData<Holiday>(profileUser.id!!, Holiday())?.let { item ->
			weekView.addHolidays(item.map {
				HolidayChip(text = it.value.longName, startDate = it.value.startDate, endDate = it.value.endDate)
			})
		}
	}

	private fun setupActionBar() {
		val toolbar: Toolbar = findViewById(R.id.toolbar_main)
		setSupportActionBar(toolbar)
		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.main_drawer_open, R.string.main_drawer_close)
		drawer.addDrawerListener(toggle)
		toggle.syncState()
	}

	private fun prepareItems(items: List<TimegridItem>): List<TimegridItem> {
		val newItems = mergeItems(items, PreferenceUtils.getPrefBool(preferences, "preference_timetable_hide_cancelled"))
		colorItems(newItems)
		return newItems
	}

	private fun mergeItems(items: List<TimegridItem>, hideCancelled: Boolean): List<TimegridItem> {
		val days = profileUser.timeGrid.days
		val itemGrid: Array<Array<MutableList<TimegridItem>>> = Array(days.size) { Array(days.maxBy { it.units.size }!!.units.size) { mutableListOf<TimegridItem>() } }

		// TODO: Check if the day from the untis API is always an english string
		val firstDayOfWeek = DateTimeConstants.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		items.forEach { item ->
			if (hideCancelled && item.periodData.isCancelled()) return@forEach

			val startDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.startDateTime)
			val endDateTime = DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(item.periodData.element.endDateTime)

			val day = endDateTime.dayOfWeek - firstDayOfWeek

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1)
				itemGrid[day][thisUnitStartIndex].add(item)
		}

		val newItems = mutableListOf<TimegridItem>()
		itemGrid.forEach { unitsOfDay ->
			unitsOfDay.forEachIndexed { unitIndex, items ->
				items.forEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}

				newItems.addAll(items)
			}
		}
		return newItems
	}

	private fun colorItems(items: List<TimegridItem>) {
		val regularColor = PreferenceUtils.getPrefInt(preferences, "preference_background_regular")
		val examColor = PreferenceUtils.getPrefInt(preferences, "preference_background_exam")
		val cancelledColor = PreferenceUtils.getPrefInt(preferences, "preference_background_cancelled")
		val irregularColor = PreferenceUtils.getPrefInt(preferences, "preference_background_irregular")

		val regularPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_regular_past")
		val examPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_exam_past")
		val cancelledPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_cancelled_past")
		val irregularPastColor = PreferenceUtils.getPrefInt(preferences, "preference_background_irregular_past")

		val useDefault = preferences.defaultPrefs.getStringSet("preference_school_background", emptySet())
				?: emptySet()
		val useTheme = if (!useDefault.contains("regular")) PreferenceUtils.getPrefBool(preferences, "preference_use_theme_background") else false

		items.forEach { item ->
			val defaultColor = Color.parseColor(item.periodData.element.backColor)

			item.color = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				useTheme -> getAttr(R.attr.colorPrimary)
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			}

			item.pastColor = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(0.25f) else examPastColor
				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(0.25f) else cancelledPastColor
				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(0.25f) else irregularPastColor
				useTheme -> getAttr(R.attr.colorPrimaryDark)
				else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
			}
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.nav_show_personal -> {
				showPersonalTimetable()
				refreshNavigationViewSelection()
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
				val i = Intent(this@MainActivity, SettingsActivity::class.java)
				i.putExtra(SettingsActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivity(i)
			}
			R.id.nav_infocenter -> {
				val i = Intent(this@MainActivity, InfoCenterActivity::class.java)
				i.putExtra(InfoCenterActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivityForResult(i, REQUEST_CODE_ROOM_FINDER)
			}
			R.id.nav_free_rooms -> {
				val i = Intent(this@MainActivity, RoomFinderActivity::class.java)
				i.putExtra(RoomFinderActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivityForResult(i, REQUEST_CODE_ROOM_FINDER)
			}
		}

		closeDrawer()
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
						@Suppress("RemoveRedundantQualifierName")
						setTarget(roomId, TimetableDatabaseInterface.Type.ROOM.toString(), timetableDatabaseInterface.getLongName(roomId, TimetableDatabaseInterface.Type.ROOM))
				}
			}
			REQUEST_CODE_SETTINGS -> {
				recreate()
			}
			REQUEST_CODE_LOGINDATAINPUT_ADD -> {
				if (resultCode == Activity.RESULT_OK)
					recreate()
			}
			REQUEST_CODE_LOGINDATAINPUT_EDIT -> {
				if (resultCode == Activity.RESULT_OK)
					recreate()
			}
		}
	}

	override fun onBackPressed() {
		val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			closeDrawer(drawer)
		} else if (!showPersonalTimetable()) {
			if (System.currentTimeMillis() - 2000 > lastBackPress && PreferenceUtils.getPrefBool(preferences, "preference_double_tap_to_exit")) {
				Snackbar.make(findViewById<ConstraintLayout>(R.id.content_main),
						R.string.main_press_back_double, 2000).show()
				lastBackPress = System.currentTimeMillis()
			} else {
				super.onBackPressed()
			}
		} else {
			refreshNavigationViewSelection()
		}
	}

	private fun setTarget(anonymous: Boolean): Boolean {
		if (anonymous) {
			showLoading(false)

			weeklyTimetableItems.clear()
			weekView.notifyDataSetChanged()

			supportActionBar?.title = getString(R.string.all_anonymous)
			constraintlayout_main_anonymouslogininfo.visibility = View.VISIBLE

			if (displayedElement == null) return false
			displayedElement = null
		} else {
			constraintlayout_main_anonymouslogininfo.visibility = View.GONE
		}
		return true
	}

	private fun setTarget(id: Int, type: String, displayName: String?): Boolean {
		PeriodElement(type, id, id).let {
			if (it == displayedElement) return false
			displayedElement = it
		}

		setTarget(anonymous = false)

		weeklyTimetableItems.clear()
		weekView.notifyDataSetChanged()
		supportActionBar?.title = displayName ?: getString(R.string.app_name)
		return true
	}

	override fun onEventClick(data: TimegridItem, eventRect: RectF) {
		showLessonInfo(data)
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
			TimetableDatabaseInterface.Type.CLASS.name -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_classes)
			TimetableDatabaseInterface.Type.TEACHER.name -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_teachers)
			TimetableDatabaseInterface.Type.ROOM.name -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_rooms)
			else -> (findViewById<View>(R.id.navigationview_main) as NavigationView)
					.setCheckedItem(R.id.nav_show_personal)
		}
	}

	private fun showLessonInfo(item: TimegridItem) {
		TimetableItemDetailsDialog.createInstance(item, timetableDatabaseInterface).show(supportFragmentManager, "itemDetails") // TODO: Remove hard-coded tag
	}

	private fun setLastRefresh(timestamp: Long) {
		textview_main_lastrefresh?.text = if (timestamp > 0L)
			getString(R.string.main_last_refreshed, formatTimeDiff(Instant.now().millis - timestamp))
		else
			getString(R.string.main_last_refreshed, getString(R.string.main_last_refreshed_never))
	}

	private fun formatTimeDiff(diff: Long): String {
		return when {
			diff < MINUTE_MILLIS -> getString(R.string.main_time_diff_just_now)
			diff < HOUR_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_minutes, ((diff / MINUTE_MILLIS).toInt()), diff / MINUTE_MILLIS)
			diff < DAY_MILLIS -> resources.getQuantityString(R.plurals.main_time_diff_hours, ((diff / HOUR_MILLIS).toInt()), diff / HOUR_MILLIS)
			else -> resources.getQuantityString(R.plurals.main_time_diff_days, ((diff / DAY_MILLIS).toInt()), diff / DAY_MILLIS)
		}
	}

	override fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
		weeklyTimetableItems[convertDateTimeToWeekIndex(startDate.toDateTime())]?.apply {
			this.items = prepareItems(items).map { it.toWeekViewEvent() }
			lastUpdated = timestamp
		}
		weekView.notifyDataSetChanged()

		// TODO: Only disable these loading indicators when everything finished loading
		showLoading(false)
	}

	override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
		when (code) {
			TimetableLoader.CODE_CACHE_MISSING -> timetableLoader.repeat(requestId, TimetableLoader.FLAG_LOAD_SERVER, proxyHost)
			else -> {
				showLoading(false)
				Snackbar.make(content_main, if (code != null) ErrorMessageDictionary.getErrorMessage(resources, code) else message
						?: getString(R.string.all_error), Snackbar.LENGTH_INDEFINITE)
						.setAction("Show") { ErrorReportingDialog(this).showRequestErrorDialog(requestId, code, message) }
						.show()
			}
		}
	}

	private fun showLoading(loading: Boolean) {
		if (!loading) swiperefreshlayout_main_timetable.isRefreshing = false
		progressbar_main_loading?.visibility = if (loading) View.VISIBLE else View.GONE
	}

	override fun onCornerClick() {
		val fragment = DatePickerDialog()

		lastPickedDate?.let {
			val args = Bundle()
			args.putInt("year", it.year)
			args.putInt("month", it.monthOfYear)
			args.putInt("day", it.dayOfMonth)
			fragment.arguments = args
		}
		fragment.dateSetListener = android.app.DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
			DateTime().withDate(year, month + 1, dayOfMonth).let {
				// +1 compensates for conversion from Calendar to DateTime
				weekView.goToDate(it)
				lastPickedDate = it
			}
		}
		fragment.show(supportFragmentManager, "datePicker")
	}

	override fun onCornerLongClick() = weekView.goToToday()

	private fun closeDrawer(drawer: DrawerLayout = findViewById(R.id.drawer_layout)) = drawer.closeDrawer(GravityCompat.START)

	private fun Int.darken(ratio: Float) = ColorUtils.blendARGB(this, Color.BLACK, ratio)

	internal class WeeklyTimetableItems {
		var items: List<WeekViewEvent<TimegridItem>> = emptyList()
		var lastUpdated: Long = 0
		var dateRange: Pair<UntisDate, UntisDate>? = null
	}
}
