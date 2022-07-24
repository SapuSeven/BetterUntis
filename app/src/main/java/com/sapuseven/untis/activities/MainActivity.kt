package com.sapuseven.untis.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.RangePreference
import com.sapuseven.untis.ui.theme.AppTheme
import com.sapuseven.untis.viewmodels.PeriodDataViewModel
import com.sapuseven.untis.views.WeekViewSwipeRefreshLayout
import com.sapuseven.untis.views.weekview.WeekView
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class MainActivity :
	BaseComposeActivity()/*,
		NavigationView.OnNavigationItemSelectedListener,
		WeekViewLoader.PeriodChangeListener<TimegridItem>,
		EventClickListener<TimegridItem>,
		TopLeftCornerClickListener,
		TimetableDisplay,
		TimetableItemDetailsFragment.TimetableItemDetailsDialogListener*/ {

	companion object {
		private const val MINUTE_MILLIS: Int = 60 * 1000
		private const val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
		private const val DAY_MILLIS: Int = 24 * HOUR_MILLIS

		private const val REQUEST_CODE_ROOM_FINDER = 1
		private const val REQUEST_CODE_SETTINGS = 2
		private const val REQUEST_CODE_LOGINDATAINPUT_ADD = 3
		private const val REQUEST_CODE_LOGINDATAINPUT_EDIT = 4
		private const val REQUEST_CODE_ERRORS = 5

		private const val UNTIS_DEFAULT_COLOR = "#f49f25"

		private const val PERSISTENT_INT_ZOOM_LEVEL = "persistent_zoom_level"

		private const val MESSENGER_PACKAGE_NAME = "com.untis.chat"

		private const val FRAGMENT_TAG_LESSON_INFO = "com.sapuseven.untis.fragments.lessoninfo"
		private const val FRAGMENT_TAG_ABSENCE_CHECK = "com.sapuseven.untis.fragments.absencecheck"

		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
	}

	private val userDatabase = UserDatabase.createInstance(this)
	private var lastBackPress: Long = 0
	private var profileId: Long = -1

	//private val weeklyTimetableItems: MutableMap<Int, WeeklyTimetableItems?> = mutableMapOf()
	private var displayedElement: PeriodElement? = null
	private var selectedElement: Int? = null
	private var lastPickedDate: DateTime? = null
	private var proxyHost: String? = null
	private var profileUpdateDialog: AlertDialog? = null
	private var currentWeekIndex = 0
	private val weekViewRefreshHandler = Handler(Looper.getMainLooper())
	private var displayNameCache: CharSequence = ""
	private var timetableLoader: TimetableLoader? = null
	private lateinit var profileUser: UserDatabase.User
	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var weekView: WeekView<TimegridItem>
	private var BOOKMARKS_ADD_ID: Int = 0
	private val timetableItemDetailsViewModel: PeriodDataViewModel by viewModels()

	private val weekViewUpdate = object : Runnable {
		override fun run() {
			weekView.invalidate()
			weekViewRefreshHandler.postDelayed(this, 60 * 1000)
		}
	}

	private val shortcutLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			// TODO: Look at it.data for potential actions (e.g. show a specific timetable)
		}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (!loadProfile())
			return

		//setupNotifications()

		setContent {
			AppTheme {
				/*weekView.setOnEventClickListener(this)
				weekView.setOnCornerClickListener(this)
				weekView.setPeriodChangeListener(this)
				weekView.scrollListener = object : ScrollListener {
					override fun onFirstVisibleDayChanged(
						newFirstVisibleDay: LocalDate,
						oldFirstVisibleDay: LocalDate?
					) {
						currentWeekIndex = convertDateTimeToWeekIndex(newFirstVisibleDay)
						setLastRefresh(
							weeklyTimetableItems[currentWeekIndex]?.lastUpdated
								?: 0
						)
					}
				}
				weekView.scaleListener = object : ScaleListener {
					override fun onScaleFinished() {
						saveZoomLevel()
					}
				}*/

				val drawerState = rememberDrawerState(DrawerValue.Closed)
				val coroutineScope = rememberCoroutineScope()

				var drawerGestures by remember { mutableStateOf(true) }

				val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)

				val navItemsElementTypes = listOf(
					NavItemTimetable(
						icon = painterResource(id = R.drawable.all_classes),
						label = stringResource(id = R.string.all_classes),
						elementType = TimetableDatabaseInterface.Type.CLASS
					),
					NavItemTimetable(
						icon = painterResource(id = R.drawable.all_teacher),
						label = stringResource(id = R.string.all_teachers),
						elementType = TimetableDatabaseInterface.Type.TEACHER
					),
					NavItemTimetable(
						icon = painterResource(id = R.drawable.all_rooms),
						label = stringResource(id = R.string.all_rooms),
						elementType = TimetableDatabaseInterface.Type.ROOM
					),
				)

				val navItemsShortcuts = listOf(
					NavItemShortcut(
						icon = painterResource(id = R.drawable.all_infocenter),
						label = stringResource(id = R.string.activity_title_info_center),
						InfoCenterActivity::class.java
					),
					/*NavItemShortcut(
						icon = painterResource(id = R.drawable.all_messenger),
						label = stringResource(id = R.string.activity_title_messenger)
					),*/
					NavItemShortcut(
						icon = painterResource(id = R.drawable.all_search_rooms),
						label = stringResource(id = R.string.activity_title_free_rooms),
						RoomFinderActivity::class.java
					),
					NavItemShortcut(
						icon = painterResource(id = R.drawable.all_settings),
						label = stringResource(id = R.string.activity_title_settings),
						SettingsActivity::class.java
					)
				)

				val selectedItem = remember { mutableStateOf(items[0]) }

				ModalNavigationDrawer(
					gesturesEnabled = drawerGestures || drawerState.isOpen,
					drawerState = drawerState,
					drawerContent = {
						/*Text(
							text = profileUser.getDisplayedName(applicationContext)
								.ifBlank { stringResource(R.string.app_name) },
							modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
						)
						Text(
							text = profileUser.userData.schoolName.ifBlank { stringResource(R.string.all_contact_email) },
							modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
						)*/
						Spacer(modifier = Modifier.height(24.dp))

						DrawerText("Favourites")

						NavigationDrawerItem(
							icon = {
								Icon(
									painterResource(id = R.drawable.all_prefs_personal),
									contentDescription = null
								)
							},
							label = { Text(stringResource(id = R.string.all_personal_timetable)) },
							selected = true,
							onClick = {
								coroutineScope.launch { drawerState.close() }
								//selectedItem.value = item
							},
							modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
						)

						NavigationDrawerItem(
							icon = {
								Icon(
									painterResource(id = R.drawable.all_add),
									contentDescription = null
								)
							},
							label = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
							selected = false,
							onClick = {
								coroutineScope.launch { drawerState.close() }
								//selectedItem.value = item
							},
							modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
						)

						DrawerText("Timetables")

						navItemsElementTypes.forEach { item ->
							NavigationDrawerItem(
								icon = { Icon(item.icon, contentDescription = null) },
								label = { Text(item.label) },
								selected = false,
								onClick = {
									coroutineScope.launch { drawerState.close() }
									//selectedItem.value = item
								},
								modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
							)
						}

						DrawerDivider()

						navItemsShortcuts.forEach { item ->
							NavigationDrawerItem(
								icon = { Icon(item.icon, contentDescription = null) },
								label = { Text(item.label) },
								selected = false,
								onClick = {
									coroutineScope.launch { drawerState.close() }
									//selectedItem.value = item

									shortcutLauncher.launch(
										Intent(
											this@MainActivity,
											item.target
										).apply {
											putExtra(EXTRA_LONG_PROFILE_ID, profileId)
										}
									)
								},
								modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
							)
						}
					}
				) {
					Scaffold(
						topBar = {
							CenterAlignedTopAppBar(
								title = { Text("BetterUntis") },
								navigationIcon = {
									IconButton(onClick = {
										coroutineScope.launch { drawerState.open() }
									}) {
										Icon(
											imageVector = Icons.Filled.Menu,
											contentDescription = "TODO"//stringResource(id = R.string.login_scan_code)
										)
									}
								},
								actions = {
									IconButton(onClick = { /*TODO*/ }) {
										Icon(
											imageVector = Icons.Filled.AccountCircle,
											contentDescription = "TODO"//stringResource(id = R.string.login_scan_code)
										)
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
							val colorScheme = MaterialTheme.colorScheme
							val currentDensity = LocalDensity.current
							val outlineAlpha = 0.4f

							AndroidView(factory = {
								val weekViewSwipeRefresh = WeekViewSwipeRefreshLayout(it)
								val weekView = WeekView<TimegridItem>(it)

								weekViewSwipeRefresh.setOnRefreshListener {
									/*displayedElement?.let { element ->
										weeklyTimetableItems[currentWeekIndex]?.dateRange?.let { dateRange ->
											loadTimetable(
												TimetableLoader.TimetableLoaderTarget(
													dateRange.first,
													dateRange.second,
													element.id,
													element.type
												), true
											)
										}
									}*/
								}

								weekView.config.apply {
									with(currentDensity) {
										daySeparatorColor =
											colorScheme.outline.copy(alpha = outlineAlpha).toArgb()
										defaultEventColor = colorScheme.primary.toArgb()
										eventMarginVertical = 4.dp.roundToPx()
										eventPadding = 4.dp.roundToPx()
										headerRowBackgroundColor = Color.TRANSPARENT
										headerRowPadding = 8.dp.roundToPx()
										headerRowSecondaryTextColor =
											colorScheme.onSurfaceVariant.toArgb()
										headerRowSecondaryTextSize = 12.sp.toPx()
										headerRowTextColor = colorScheme.onSurface.toArgb()
										headerRowTextSize = 18.sp.toPx()
										headerRowTextSpacing = 10.dp.roundToPx()
										holidayTextColor = colorScheme.onSurface.toArgb()
										holidayTextSize = 16.sp.toPx()
										hourHeight = 72.dp.roundToPx()
										hourSeparatorColor =
											colorScheme.outline.copy(alpha = outlineAlpha).toArgb()
										nowLineStrokeWidth = 2.dp.toPx()
										scrollDuration = 100
										showHourSeparator = true
										showNowLine = true
										timeColumnBackground = Color.TRANSPARENT
										timeColumnCaptionColor = colorScheme.onSurface.toArgb()
										timeColumnCaptionSize = 16.sp.toPx()
										timeColumnPadding = 4.dp.roundToPx()
										timeColumnTextColor = colorScheme.onSurfaceVariant.toArgb()
										timeColumnTextSize = 12.sp.toPx()
										todayHeaderTextColor = colorScheme.primary.toArgb()
										topLeftCornerDrawable = AppCompatResources.getDrawable(
											this@MainActivity,
											R.drawable.all_calendar_adjusted
										)
										topLeftCornerPadding = 4.dp.roundToPx()
										topLeftCornerTint = colorScheme.onSurface.toArgb()
									}
								}

								weekView.apply {
									setupCustomization()
									setupHours()

									// Workaround to enable drawer gestures only when swiping from the left edge (won't work with RTL layout)
									onMotionEvent = { event ->
										when (event.action) {
											MotionEvent.ACTION_DOWN -> {
												drawerGestures =
													event.x < with(currentDensity) { 48.dp.toPx() }
											}
											MotionEvent.ACTION_UP -> {
												drawerGestures = true
											}
										}
									}
								}

								weekViewSwipeRefresh.apply {
									addView(weekView)
								}
							}, modifier = Modifier.fillMaxSize())
						}
					}
				}
			}
		}

		/*if (checkForCrashes()) {
			startActivityForResult(Intent(this, ErrorsActivity::class.java).apply {
				putExtra(ErrorsActivity.EXTRA_BOOLEAN_SHOW_CRASH_MESSAGE, true)
			}, REQUEST_CODE_ERRORS)
		} else {
			setupActionBar()
			setupNavDrawer()

			setupViews()
			setupHours()
			setupHolidays()

			setupTimetableLoader()
			if (!checkShortcut()) showPersonalTimetable()
			refreshNavigationViewSelection()
		}*/
	}

	@Composable
	fun DrawerDivider() {
		Divider(
			color = MaterialTheme.colorScheme.outline,
			modifier = Modifier.padding(vertical = 8.dp)
		)
	}

	@Composable
	fun DrawerText(text: String) {
		Text(
			text = text,
			style = MaterialTheme.typography.labelMedium,
			modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 8.dp)
		)
	}

	abstract class NavItem(
		open val icon: Painter,
		open val label: String
	)

	data class NavItemTimetable(
		override val icon: Painter,
		override val label: String,
		val elementType: TimetableDatabaseInterface.Type
	) : NavItem(icon, label)

	data class NavItemShortcut(
		override val icon: Painter,
		override val label: String,
		val target: Class<*>
	) : NavItem(icon, label)

	/*private fun checkForProfileUpdateRequired(): Boolean {
		return profileUser.schoolId.isBlank() || profileUser.apiUrl.isBlank()
	}

	private fun checkForNewSchoolYear(): Boolean {
		val currentSchoolYearId = getCurrentSchoolYear()?.id ?: -1

		if (!preferences.has("school_year"))
		preferences["school_year"] = currentSchoolYearId

		return if (preferences["school_year", -1] != currentSchoolYearId) {
			preferences.has("school_year")
		} else false
	}

	private fun getCurrentSchoolYear(): SchoolYear? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(
			profileUser.id
				?: -1, SchoolYear()
		)?.values?.toList() ?: emptyList()

		return schoolYears.find {
			val now = LocalDate.now()
			now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
		}
	}

	override fun onPause() {
		weekViewRefreshHandler.removeCallbacks(weekViewUpdate)
		super.onPause()
	}

	override fun onResume() {
		super.onResume()
		if (timetableLoader == null) return

		refreshMessages(profileUser, navigationview_main)

		if (::weekView.isInitialized) {
			proxyHost = preferences["preference_connectivity_proxy_host", null]
			setupWeekViewConfig()

			weekViewRefreshHandler.post(weekViewUpdate)
		}
	}

	override fun onErrorLogFound() {
		// TODO: Extract string resources
		if (preferences["preference_additional_error_messages"])
			Snackbar.make(content_main, "Some errors have been found.", Snackbar.LENGTH_INDEFINITE)
				.setAction("Show") {
					startActivity(Intent(this, ErrorsActivity::class.java))
				}
				.show()
	}

	private fun showProfileUpdateRequired() {
		profileUpdateDialog = MaterialAlertDialogBuilder(this)
			.setTitle(getString(R.string.main_dialog_update_profile_title))
			.setMessage(getString(R.string.main_dialog_update_profile_message))
			.setPositiveButton(getString(R.string.main_dialog_update_profile_button)) { _, _ ->
				updateProfile(profileUser)
			}
			.setCancelable(false)
			.show()
	}*/

	private fun login() {
		val loginIntent = Intent(this, LoginActivity::class.java)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_ADD)
		finish()
	}

	/*private fun showPersonalTimetable(): Boolean {
		val customType = TimetableDatabaseInterface.Type.valueOf(
				preferences["preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_TYPE}",
					TimetableDatabaseInterface.Type.SUBJECT.toString()]
		)
		selectedElement = R.id.nav_show_personal
		if (customType === TimetableDatabaseInterface.Type.SUBJECT) {
			profileUser.userData.elemType?.let { type ->
				return setTarget(
					profileUser.userData.elemId,
					type,
					profileUser.getDisplayedName(applicationContext)
				)
			} ?: run {
				return setTarget(true, profileUser.getDisplayedName(applicationContext))
			}
		} else {
			val customId = preferences[
				"preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_ID}",
				-1
			]
			return setTarget(
				customId,
				customType.toString(),
				timetableDatabaseInterface.getLongName(customId, customType)
			)
		}
	}

	private fun checkShortcut(): Boolean {
		return intent.extras?.let { extras ->
			val userId = extras.getLong("user")
			if (preferences.loadProfileId() != userId)
				switchToProfile(userDatabase.getUser(userId) ?: return false)

			val element = PeriodElement(
				type = extras.getString("type") ?: return false,
				id = extras.getInt("id"),
				orgId = extras.getInt("orgId")
			)
			val useOrgId = extras.getBoolean("useOrgId")
			setTarget(
				if (useOrgId) element.orgId else element.id,
				element.type,
				timetableDatabaseInterface.getLongName(
					if (useOrgId) element.orgId else element.id,
					TimetableDatabaseInterface.Type.valueOf(element.type)
				)
			)
			true
		} ?: false
	}

	private fun setupNotifications() {
		val intent = Intent(this, StartupReceiver::class.java)
		intent.putExtra(EXTRA_BOOLEAN_MANUAL, true)
		sendBroadcast(intent)
	}

	private fun setupTimetableLoader() {
		timetableLoader =
			TimetableLoader(WeakReference(this), this, profileUser, timetableDatabaseInterface)
	}

	private fun setupNavDrawer() {
		navigationview_main.setNavigationItemSelectedListener(this)
		navigationview_main.setCheckedItem(R.id.nav_show_personal)

		updateNavDrawer(navigationview_main)

		val header = navigationview_main.getHeaderView(0)
		val dropdown =
			header.findViewById<ConstraintLayout>(R.id.constraintlayout_mainactivitydrawer_dropdown)
		val dropdownView =
			header.findViewById<LinearLayout>(R.id.linearlayout_mainactivitydrawer_dropdown_view)
		val dropdownImage =
			header.findViewById<ImageView>(R.id.imageview_mainactivitydrawer_dropdown_arrow)
		val dropdownList =
			header.findViewById<RecyclerView>(R.id.recyclerview_mainactivitydrawer_profile_list)

		profileListAdapter = ProfileListAdapter(
			this,
			userDatabase.getAllUsers().toMutableList(),
			{ view ->
				toggleProfileDropdown(dropdownView, dropdownImage, dropdownList)
				switchToProfile(profileListAdapter.itemAt(dropdownList.getChildLayoutPosition(view)))
			},
			{ view ->
				closeDrawer()
				editProfile(profileListAdapter.itemAt(dropdownList.getChildLayoutPosition(view)))
				true
			})
		dropdownList.adapter = profileListAdapter
		dropdown.setOnClickListener {
			toggleProfileDropdown(dropdownView, dropdownImage, dropdownList)
		}

		val profileListAdd =
			header.findViewById<ConstraintLayout>(R.id.constraintlayout_mainactivitydrawer_add)
		profileListAdd.setOnClickListener {
			closeDrawer()
			addProfile()
		}
	}

	private fun updateNavDrawer(navigationView: NavigationView) {
		val line1 = profileUser.getDisplayedName(applicationContext)
		val line2 = profileUser.userData.schoolName
		(navigationView.getHeaderView(0)
			.findViewById<View>(R.id.textview_mainactivtydrawer_line1) as TextView).text =
				line1.ifBlank { getString(R.string.app_name) }
		(navigationView.getHeaderView(0)
			.findViewById<View>(R.id.textview_mainactivitydrawer_line2) as TextView).text =
				line2.ifBlank { getString(R.string.all_contact_email) }

		navigationView.menu.findItem(R.id.nav_messenger).isVisible = false
	}

	private fun toggleProfileDropdown(
		dropdownView: ViewGroup,
		dropdownImage: ImageView,
		dropdownList: RecyclerView
	) {
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
			.putExtra(LoginDataInputActivity.EXTRA_LONG_PROFILE_ID, user.id)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_EDIT)
	}

	private fun updateProfile(user: UserDatabase.User) {
		val loginIntent = Intent(this, LoginDataInputActivity::class.java)
			.putExtra(LoginDataInputActivity.EXTRA_LONG_PROFILE_ID, user.id)
			.putExtra(EXTRA_BOOLEAN_PROFILE_UPDATE, true)
		startActivityForResult(loginIntent, REQUEST_CODE_LOGINDATAINPUT_EDIT)
	}

	private fun switchToProfile(user: UserDatabase.User) {
		profileId = user.id!!
		preferences.saveProfileId(profileId)
		if (loadProfile()) {
			updateNavDrawer(findViewById(R.id.navigationview_main))

			closeDrawer()
			setupTimetableLoader()
			showPersonalTimetable()
			refreshNavigationViewSelection()

			recreate()
		} else {
			timetableLoader = null
		}
	}

	private fun refreshMessages(user: UserDatabase.User, navigationView: NavigationView) =
		GlobalScope.launch(Dispatchers.Main) {
			loadMessages(user)?.let {
				navigationView.menu.findItem(R.id.nav_infocenter).icon = if (
					it.size > preferences["preference_last_messages_count", 0] ||
					(SimpleDateFormat(
						"dd-MM-yyyy",
						Locale.US
					).format(Calendar.getInstance().time) != preferences["preference_last_messages_date", ""] && it.isNotEmpty())
				) {
					getDrawable(R.drawable.all_infocenter_dot)
				} else {
					getDrawable(R.drawable.all_infocenter)
				}
			}
		}

	//TODO: Duplicated function from info center
	private suspend fun loadMessages(user: UserDatabase.User): List<UntisMessage>? {

		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost =
			preferences["preference_connectivity_proxy_host", null]
		query.data.params = listOf(
			MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = UntisRequest().request(query)
		return result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<MessageResponse>(data)

			untisResponse.result?.messages
		}, { null })
	}

	private fun setupViews() {
		setupWeekView()
		restoreZoomLevel()

		textview_main_lastrefresh?.text =
			getString(R.string.main_last_refreshed, getString(R.string.main_last_refreshed_never))

		button_main_settings.setOnClickListener {
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
					loadTimetable(
						TimetableLoader.TimetableLoaderTarget(
							dateRange.first,
							dateRange.second,
							element.id,
							element.type
						), true
					)
				}
			}
		}
	}

	private fun loadTimetable(
		target: TimetableLoader.TimetableLoaderTarget,
		forceRefresh: Boolean = false
	) {
		if (timetableLoader == null) return

		weekView.notifyDataSetChanged()
		if (!forceRefresh) showLoading(true)

		val alwaysLoad: Boolean = preferences["preference_connectivity_refresh_in_background"]
		val flags =
			(if (!forceRefresh) TimetableLoader.FLAG_LOAD_CACHE else 0) or (if (alwaysLoad || forceRefresh) TimetableLoader.FLAG_LOAD_SERVER else 0)
		timetableLoader!!.load(target, flags, proxyHost)
	}*/

	private fun loadProfile(): Boolean {
		if (userDatabase.getUsersCount() < 1) {
			login()
			return false
		}

		profileId = preferences.loadProfileId()
		if (profileId == 0L || userDatabase.getUser(profileId) == null)
			profileId = userDatabase.getAllUsers()[0].id
				?: 0 // Fall back to the first user if an invalid user id is saved
		profileUser = userDatabase.getUser(profileId) ?: return false

		preferences.saveProfileId(profileId)
		preferences.loadProfile(profileId)
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser.id ?: 0)

		/*if (checkForProfileUpdateRequired()) {
			showProfileUpdateRequired()
			return false
		}

		if (checkForNewSchoolYear()) {
			updateProfile(profileUser)
			return false
		}*/

		return true
	}

	/*private fun setupWeekView() {
		weekView = findViewById(R.id.weekview_main_timetable)
		weekView.setOnEventClickListener(this)
		weekView.setOnCornerClickListener(this)
		weekView.setPeriodChangeListener(this)
		weekView.scrollListener = object : ScrollListener {
			override fun onFirstVisibleDayChanged(
				newFirstVisibleDay: LocalDate,
				oldFirstVisibleDay: LocalDate?
			) {
				currentWeekIndex = convertDateTimeToWeekIndex(newFirstVisibleDay)
				setLastRefresh(
					weeklyTimetableItems[currentWeekIndex]?.lastUpdated
						?: 0
				)
			}
		}
		weekView.scaleListener = object : ScaleListener {
			override fun onScaleFinished() {
				saveZoomLevel()
			}
		}
		setupWeekViewConfig()
	}

	private fun saveZoomLevel() {
		preferences[PERSISTENT_INT_ZOOM_LEVEL] = weekView.hourHeight
	}

	private fun restoreZoomLevel() {
		weekView.hourHeight = preferences[PERSISTENT_INT_ZOOM_LEVEL, weekView.hourHeight]
	}

	override fun onPeriodChange(
		startDate: LocalDate,
		endDate: LocalDate
	): List<WeekViewDisplayable<TimegridItem>> {
		val weekIndex = convertDateTimeToWeekIndex(startDate)
		return weeklyTimetableItems[weekIndex]?.items ?: run {
			displayedElement?.let { displayedElement ->
				weeklyTimetableItems[weekIndex] = WeeklyTimetableItems().apply {
					dateRange =
						(UntisDate.fromLocalDate(LocalDate(startDate)) to UntisDate.fromLocalDate(
							LocalDate(endDate)
						)).also { dateRange ->
							loadTimetable(
								TimetableLoader.TimetableLoaderTarget(
									dateRange.first,
									dateRange.second,
									displayedElement.id,
									displayedElement.type
								)
							)
						}
				}
			}
			emptyList()
		}
	}

	private fun convertDateTimeToWeekIndex(date: LocalDate) = date.year * 100 + date.dayOfYear / 7

	private fun setupHolidays() {
		userDatabase.getAdditionalUserData<Holiday>(profileUser.id!!, Holiday())?.let { item ->
			weekView.addHolidays(item.map {
				HolidayChip(
					text = it.value.longName,
					startDate = it.value.startDate,
					endDate = it.value.endDate
				)
			})
		}
	}

	private fun setupActionBar() {
		setSupportActionBar(toolbar_main)
		val toggle = ActionBarDrawerToggle(
			this,
			drawer_layout,
			toolbar_main,
			R.string.main_drawer_open,
			R.string.main_drawer_close
		)
		drawer_layout.addDrawerListener(toggle)
		toolbar_main.setNavigationOnClickListener { setBookmarksLongClickListeners() ; openDrawer() }
		toggle.syncState()
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
		window.statusBarColor = Color.TRANSPARENT
		supportFragmentManager.addOnBackStackChangedListener {
			if (supportFragmentManager.backStackEntryCount > 0) {
				toggle.isDrawerIndicatorEnabled = false
				supportActionBar?.setDisplayHomeAsUpEnabled(true)
				drawer_layout.setDrawerLockMode(
					DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
					GravityCompat.START
				)
				toolbar_main.setNavigationOnClickListener { onBackPressed() }
				// TODO: Set actionBar title to match fragment
			} else {
				supportActionBar?.setDisplayHomeAsUpEnabled(false)
				toggle.isDrawerIndicatorEnabled = true
				toggle.syncState()
				drawer_layout.setDrawerLockMode(
					DrawerLayout.LOCK_MODE_UNLOCKED,
					GravityCompat.START
				)
				toolbar_main.setNavigationOnClickListener {  setBookmarksLongClickListeners() ; openDrawer() }
				// TODO: Set actionBar title to default
			}
		}
	}

	override fun onPrepareOptionsMenu(menu: Menu): Boolean {
		var i = 0
		navigationview_main.menu.findItem(R.id.nav_personal_bookmarks_title).subMenu?.let {
			// remove everything except personal timetable (in case menu has been invalidated)
			for (index in 0 until it.size()) {
				it.removeItem(index)
			}
			userDatabase.getUser(profileId)?.bookmarks?.forEach { bookmark ->
				it.add(0, i, Menu.FIRST + i, bookmark.displayName)
					.setIcon(bookmark.drawableId).isCheckable = true
				++i
			}
			BOOKMARKS_ADD_ID = i
			it.add(0, BOOKMARKS_ADD_ID, Menu.FIRST + i, getString(R.string.maindrawer_bookmarks_add)).setIcon(getDrawable(R.drawable.all_add))
			refreshNavigationViewSelection()
		}
		return super.onPrepareOptionsMenu(menu)
	}

	private fun prepareItems(items: List<TimegridItem>): List<TimegridItem> {
		val newItems = mergeItems(items.mapNotNull { item ->
			if (preferences["preference_timetable_hide_cancelled"] && item.periodData.isCancelled())
				return@mapNotNull null

			if (preferences["preference_timetable_substitutions_irregular"]) {
				item.periodData.apply {
					forceIrregular =
						classes.find { it.id != it.orgId } != null
								|| teachers.find { it.id != it.orgId } != null
								|| subjects.find { it.id != it.orgId } != null
								|| rooms.find { it.id != it.orgId } != null
								|| preferences["preference_timetable_background_irregular"]
								&& item.periodData.element.backColor != UNTIS_DEFAULT_COLOR
				}
			}
			item
		})
		colorItems(newItems)
		return newItems
	}

	private fun mergeItems(items: List<TimegridItem>): List<TimegridItem> {
		val days = profileUser.timeGrid.days
		val itemGrid: Array<Array<MutableList<TimegridItem>>> =
			Array(days.size) { Array(days.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<TimegridItem> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DateTimeConstants.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		items.forEach { item ->
			val startDateTime = item.periodData.element.startDateTime.toLocalDateTime()
			val endDateTime = item.periodData.element.endDateTime.toLocalDateTime()

			val day = endDateTime.dayOfWeek - firstDayOfWeek

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime.time == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime.time == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1)
				itemGrid[day][thisUnitStartIndex].add(item)
			else
				leftover.add(item)
		}

		val newItems = mutableListOf<TimegridItem>()
		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.
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
		val regularColor = preferences.get<Int>("preference_background_regular")
		val examColor = preferences.get<Int>("preference_background_exam")
		val cancelledColor =
			preferences.get<Int>("preference_background_cancelled")
		val irregularColor =
			preferences.get<Int>("preference_background_irregular")

		val regularPastColor =
			preferences.get<Int>("preference_background_regular_past")
		val examPastColor =
			preferences.get<Int>("preference_background_exam_past")
		val cancelledPastColor =
			preferences.get<Int>("preference_background_cancelled_past")
		val irregularPastColor =
			preferences.get<Int>("preference_background_irregular_past")

		val useDefault =
			preferences.sharedPrefs!!.getStringSet("preference_school_background", emptySet())
				?: emptySet()
		val useTheme = if (!useDefault.contains("regular")) preferences["preference_use_theme_background"] else false

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
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(
					0.25f
				) else examPastColor
				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(
					0.25f
				) else cancelledPastColor
				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(
					0.25f
				) else irregularPastColor
				useTheme -> if (currentTheme == "pixel") getAttr(R.attr.colorPrimary).darken(0.25f) else getAttr(
					R.attr.colorPrimaryDark
				)
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
			BOOKMARKS_ADD_ID -> {
				ElementPickerDialog.newInstance(
						timetableDatabaseInterface,
						ElementPickerDialog.Companion.ElementPickerDialogConfig(TimetableDatabaseInterface.Type.CLASS),
						object: ElementPickerDialog.ElementPickerDialogListener {
							override fun onDialogDismissed(dialog: DialogInterface?) { /* ignore */ }

							override fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean) {
								if(fragment is DialogFragment)
									fragment.dismiss()
								else
									removeFragment(fragment)
								val user = userDatabase.getUser(profileId)
								if(user != null) {
									element?.let {
										user.bookmarks = user.bookmarks.plus(TimetableBookmark(it.id, it.type, timetableDatabaseInterface.getShortName(it.id,
												TimetableDatabaseInterface.Type.valueOf(it.type)),
												when(TimetableDatabaseInterface.Type.valueOf(it.type)) {
													TimetableDatabaseInterface.Type.CLASS -> R.drawable.all_classes
													TimetableDatabaseInterface.Type.ROOM -> R.drawable.all_rooms
													TimetableDatabaseInterface.Type.TEACHER -> R.drawable.all_teacher
													TimetableDatabaseInterface.Type.SUBJECT -> R.drawable.all_subject }))
										userDatabase.editUser(user)
										updateNavDrawer(findViewById(R.id.navigationview_main))
										selectedElement = user.bookmarks.size - 1
										invalidateOptionsMenu()
										setTarget(it.id,it.type,timetableDatabaseInterface.getLongName(it.id, TimetableDatabaseInterface.Type.valueOf(it.type)))
									}
								}
							}

							override fun onPositiveButtonClicked(dialog: ElementPickerDialog) { /* not used */ }

						}

				).show(supportFragmentManager, "elementPicker")
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
				startActivityForResult(i, REQUEST_CODE_SETTINGS)
			}
			R.id.nav_infocenter -> {
				val i = Intent(this@MainActivity, InfoCenterActivity::class.java)
				i.putExtra(InfoCenterActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivityForResult(i, REQUEST_CODE_ROOM_FINDER)
			}
			R.id.nav_messenger -> {
				try {
					startActivity(packageManager.getLaunchIntentForPackage(MESSENGER_PACKAGE_NAME))
				} catch (e: Exception) {
					try {
						startActivity(
							Intent(
								Intent.ACTION_VIEW,
								Uri.parse("market://details?id=$MESSENGER_PACKAGE_NAME")
							)
						)
					} catch (e: Exception) {
						startActivity(
							Intent(
								Intent.ACTION_VIEW,
								Uri.parse("https://play.google.com/store/apps/details?id=$MESSENGER_PACKAGE_NAME")
							)
						)
					}
				}
			}
			R.id.nav_free_rooms -> {
				val i = Intent(this@MainActivity, RoomFinderActivity::class.java)
				i.putExtra(RoomFinderActivity.EXTRA_LONG_PROFILE_ID, profileId)
				startActivityForResult(i, REQUEST_CODE_ROOM_FINDER)
			}
			else -> {
				val bookmarks = userDatabase.getUser(profileId)?.bookmarks
				if (bookmarks != null) {
					if(item.itemId < bookmarks.size){
						val target = bookmarks[item.itemId]
						selectedElement = item.itemId
						setTarget(target.classId,target.type,timetableDatabaseInterface.getLongName(target.classId, TimetableDatabaseInterface.Type.valueOf(target.type)))
					}
				}
			}
		}
		closeDrawer()
		return true
	}

	private fun showItemList(type: TimetableDatabaseInterface.Type) {
		ElementPickerDialog.newInstance(
				timetableDatabaseInterface,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(type),
				object: ElementPickerDialog.ElementPickerDialogListener {
					override fun onDialogDismissed(dialog: DialogInterface?) { refreshNavigationViewSelection() }

					override fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean) {
						if (fragment is DialogFragment)
							fragment.dismiss()
						else
							removeFragment(fragment)
						element?.let {
							setTarget(if (useOrgId) element.orgId else element.id, element.type, timetableDatabaseInterface.getLongName(
									if (useOrgId) element.orgId else element.id, TimetableDatabaseInterface.Type.valueOf(element.type)))
						} ?: run {
							showPersonalTimetable()
						}
						selectedElement = when (element?.type) {
							TimetableDatabaseInterface.Type.CLASS.name -> R.id.nav_show_classes
							TimetableDatabaseInterface.Type.TEACHER.name -> R.id.nav_show_teachers
							TimetableDatabaseInterface.Type.ROOM.name -> R.id.nav_show_rooms
							else -> {R.id.nav_show_personal}
						}
						refreshNavigationViewSelection()
					}

					override fun onPositiveButtonClicked(dialog: ElementPickerDialog) { /* not used */ }

				}
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)

		when (requestCode) {
			REQUEST_CODE_ROOM_FINDER ->
				if (resultCode == Activity.RESULT_OK)
					(data?.getIntExtra(RoomFinderActivity.EXTRA_INT_ROOM_ID, -1)
						?: -1).let { roomId ->
						if (roomId != -1)
							@Suppress("RemoveRedundantQualifierName")
							setTarget(
								roomId,
								TimetableDatabaseInterface.Type.ROOM.toString(),
								timetableDatabaseInterface.getLongName(
									roomId,
									TimetableDatabaseInterface.Type.ROOM
								)
							)
					}
			REQUEST_CODE_SETTINGS -> recreate()
			REQUEST_CODE_LOGINDATAINPUT_ADD ->
				if (resultCode == Activity.RESULT_OK)
					recreate()
			REQUEST_CODE_LOGINDATAINPUT_EDIT ->
				if (resultCode == Activity.RESULT_OK) {
					recreate()
				} else if (checkForNewSchoolYear()) {
					finish()
				}
			REQUEST_CODE_ERRORS -> recreate()
		}
	}

	override fun onBackPressed() {
		if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
			closeDrawer(drawer_layout)
		} else if (supportFragmentManager.backStackEntryCount > 0) {
			super.onBackPressed()
		} else if (!showPersonalTimetable()) {
			if (System.currentTimeMillis() - 2000 > lastBackPress && preferences["preference_double_tap_to_exit"]) {
				Snackbar.make(
					content_main,
					R.string.main_press_back_double, 2000
				).show()
				lastBackPress = System.currentTimeMillis()
			} else {
				super.onBackPressed()
			}
		} else {
			refreshNavigationViewSelection()
		}
	}

	private fun setTarget(anonymous: Boolean, displayName: CharSequence): Boolean {
		supportActionBar?.title = displayName
		if (anonymous) {
			showLoading(false)

			weeklyTimetableItems.clear()
			weekView.notifyDataSetChanged()

			constraintlayout_main_anonymouslogininfo.visibility = View.VISIBLE

			if (displayedElement == null) return false
			displayedElement = null
		} else {
			constraintlayout_main_anonymouslogininfo.visibility = View.GONE
		}
		return true
	}

	private fun setTarget(id: Int, type: String, displayName: String?): Boolean {
		displayNameCache = displayName ?: getString(R.string.app_name)
		PeriodElement(type, id, id).let {
			if (it == displayedElement) return false
			displayedElement = it
		}

		setTarget(false, displayNameCache)

		weeklyTimetableItems.clear()
		weekView.notifyDataSetChanged()
		return true
	}

	internal fun setFullscreenDialogActionBar() {
		supportActionBar?.setHomeAsUpIndicator(R.drawable.all_close)
		supportActionBar?.setTitle(R.string.all_lesson_details)
	}

	internal fun setDefaultActionBar() {
		supportActionBar?.title = displayNameCache
	}

	override fun onEventClick(data: TimegridItem, eventRect: RectF) {
		viewModelStore.clear() // TODO: Doesn't seem like the best solution. This could potentially interfere with other ViewModels scoped to this activity.
		val fragment = TimetableItemDetailsFragment(data, timetableDatabaseInterface, profileUser)

		supportFragmentManager.beginTransaction().run {
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			add(R.id.content_main, fragment, FRAGMENT_TAG_LESSON_INFO)
			addToBackStack(fragment.tag)
			commit()
		}
	}

	override fun onPeriodElementClick(
		fragment: Fragment,
		element: PeriodElement?,
		useOrgId: Boolean
	) {
		if (fragment is DialogFragment)
			fragment.dismiss()
		else
			removeFragment(fragment)
		element?.let {
			setTarget(
				if (useOrgId) element.orgId else element.id,
				element.type,
				timetableDatabaseInterface.getLongName(
					if (useOrgId) element.orgId else element.id,
					TimetableDatabaseInterface.Type.valueOf(element.type)
				)
			)
		} ?: run {
			showPersonalTimetable()
		}
		refreshNavigationViewSelection()
	}

	override fun onPeriodAbsencesClick() {
		val absenceEditFragment = AbsenceCheckFragment()

		supportFragmentManager.beginTransaction().run {
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			add(R.id.content_main, absenceEditFragment, FRAGMENT_TAG_ABSENCE_CHECK)
			addToBackStack(absenceEditFragment.tag)
			commit()
		}
	}

	override fun onLessonTopicClick() {
		val dialogView = layoutInflater.inflate(R.layout.dialog_edit_lessontopic, null)
		val etLessonTopic = dialogView.findViewById<TextInputEditText>(R.id.edittext_dialog)

		etLessonTopic.setText(timetableItemDetailsViewModel.periodData().value?.topic?.text ?: "")

		MaterialAlertDialogBuilder(this)
			.setView(dialogView)
			.setPositiveButton(R.string.all_ok) { dialog, _ ->
				val lessonTopic = etLessonTopic.text.toString()
				timetableItemDetailsViewModel.submitLessonTopic(lessonTopic)
				dialog.dismiss()
			}
			.show()
	}


	private fun removeFragment(fragment: Fragment) {
		supportFragmentManager.popBackStack(fragment.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}

	private fun refreshNavigationViewSelection() {
		selectedElement?.let { (navigationview_main as NavigationView).setCheckedItem(it) }
	}

	private fun setLastRefresh(timestamp: Long) {
		textview_main_lastrefresh?.text = if (timestamp > 0L)
			getString(
				R.string.main_last_refreshed,
				formatTimeDiff(Instant.now().millis - timestamp)
			)
		else
			getString(R.string.main_last_refreshed, getString(R.string.main_last_refreshed_never))
	}

	private fun formatTimeDiff(diff: Long): String {
		return when {
			diff < MINUTE_MILLIS -> getString(R.string.main_time_diff_just_now)
			diff < HOUR_MILLIS -> resources.getQuantityString(
				R.plurals.main_time_diff_minutes,
				((diff / MINUTE_MILLIS).toInt()),
				diff / MINUTE_MILLIS
			)
			diff < DAY_MILLIS -> resources.getQuantityString(
				R.plurals.main_time_diff_hours,
				((diff / HOUR_MILLIS).toInt()),
				diff / HOUR_MILLIS
			)
			else -> resources.getQuantityString(
				R.plurals.main_time_diff_days,
				((diff / DAY_MILLIS).toInt()),
				diff / DAY_MILLIS
			)
		}
	}

	override fun addTimetableItems(
		items: List<TimegridItem>,
		startDate: UntisDate,
		endDate: UntisDate,
		timestamp: Long
	) {
		for (item in items) {
			if (item.periodData.element.messengerChannel != null) {
				navigationview_main.menu.findItem(R.id.nav_messenger).isVisible = true
				break
			}
		}

		weeklyTimetableItems[convertDateTimeToWeekIndex(startDate.toLocalDate())]?.apply {
			this.items = prepareItems(items).map { it.toWeekViewEvent() }
			lastUpdated = timestamp
		}
		weekView.notifyDataSetChanged()

		// TODO: Only disable these loading indicators when everything finished loading
		showLoading(false)
	}

	override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
		if (timetableLoader == null) return

		when (code) {
			TimetableLoader.CODE_CACHE_MISSING -> timetableLoader!!.repeat(
				requestId,
				TimetableLoader.FLAG_LOAD_SERVER,
				proxyHost
			)
			else -> {
				showLoading(false)
				Snackbar.make(
					content_main,
					if (code != null) ErrorMessageDictionary.getErrorMessage(
						resources,
						code
					) else message
						?: getString(R.string.all_error),
					Snackbar.LENGTH_INDEFINITE
				)
					.setAction("Show") {
						ErrorReportingDialog(this).showRequestErrorDialog(
							requestId,
							code,
							message
						)
					}
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
		fragment.dateSetListener =
			android.app.DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
				DateTime().withDate(year, month + 1, dayOfMonth).let {
					// +1 compensates for conversion from Calendar to DateTime
					weekView.goToDate(it)
					lastPickedDate = it
				}
			}
		fragment.show(supportFragmentManager, "datePicker")
	}

	override fun onCornerLongClick() = weekView.goToToday()

	private fun openDrawer(drawer: DrawerLayout = drawer_layout) =
		drawer.openDrawer(GravityCompat.START)

	private fun closeDrawer(drawer: DrawerLayout = drawer_layout) =
		drawer.closeDrawer(GravityCompat.START)

	private fun Int.darken(ratio: Float) = ColorUtils.blendARGB(this, Color.BLACK, ratio)

	internal class WeeklyTimetableItems {
		var items: List<WeekViewEvent<TimegridItem>> = emptyList()
		var lastUpdated: Long = 0
		var dateRange: Pair<UntisDate, UntisDate>? = null
	}

	private fun setBookmarksLongClickListeners() {
		(navigationview_main[0] as RecyclerView).let { rv ->
			rv.post {
				for (index in 3..rv.layoutManager?.itemCount!!) {
					val bookmarks = userDatabase.getUser(profileId)?.bookmarks
					if(bookmarks != null){
						if(index-3 < bookmarks.size) {
							rv.layoutManager?.findViewByPosition(index)?.setOnLongClickListener {
								Log.d("Bookmark", "$index")
								closeDrawer()
								MaterialAlertDialogBuilder(this).setMessage(getString(R.string.main_dialog_delete_bookmark))
										.setPositiveButton(getString(R.string.all_yes)) { _, _ ->
											userDatabase.getUser(profileId)?.let { user ->
												if(selectedElement == index-3) {
													showPersonalTimetable()
												}
												user.bookmarks = user.bookmarks.minus(bookmarks[index-3])
												if(selectedElement!! < bookmarks.size && selectedElement!! > index-3){
													selectedElement = selectedElement!! - 1
												}
												userDatabase.editUser(user)
												invalidateOptionsMenu()
											}
										}
										.setNegativeButton(getString(R.string.all_no)) { _, _ -> }
										.show()
								true
							}
						}
					}

				}
			}
		}
	}*/

	private fun <T> WeekView<T>.setupCustomization() {
		weekLength = preferences.sharedPrefs!!.getStringSet(
			"preference_week_custom_range",
			emptySet()
		)?.size?.zeroToNull ?: profileUser.timeGrid.days.size
		numberOfVisibleDays =
			preferences["preference_week_custom_display_length", 0].zeroToNull
				?: weekLength
		firstDayOfWeek = preferences.sharedPrefs!!.getStringSet(
			"preference_week_custom_range",
			emptySet()
		)?.map { MaterialDayPicker.Weekday.valueOf(it) }
			?.minOrNull()?.ordinal ?: DateTimeFormat.forPattern("E")
			.withLocale(Locale.ENGLISH) // TODO: Correct locale?
			.parseDateTime(profileUser.timeGrid.days[0].day).dayOfWeek
		timeColumnVisibility =
			!preferences.get<Boolean>("preference_timetable_hide_time_stamps")
		columnGap = ConversionUtils.dpToPx(
			preferences.get<Int>("preference_timetable_item_padding")
				.toFloat(), this@MainActivity
		).toInt()
		overlappingEventGap = ConversionUtils.dpToPx(
			preferences.get<Int>("preference_timetable_item_padding_overlap")
				.toFloat(), this@MainActivity
		).toInt()
		eventCornerRadius = ConversionUtils.dpToPx(
			preferences.get<Int>("preference_timetable_item_corner_radius")
				.toFloat(), this@MainActivity
		).toInt()
		eventSecondaryTextCentered =
			preferences["preference_timetable_centered_lesson_info"]
		eventTextBold =
			preferences["preference_timetable_bold_lesson_name"]
		eventTextSize = ConversionUtils.spToPx(
			preferences.get<Int>("preference_timetable_lesson_name_font_size")
				.toFloat(), this@MainActivity
		)
		eventSecondaryTextSize = ConversionUtils.spToPx(
			preferences.get<Int>("preference_timetable_lesson_info_font_size")
				.toFloat(), this@MainActivity
		)
		eventTextColor =
			if (preferences["preference_timetable_item_text_light"]) Color.WHITE else Color.BLACK
		pastBackgroundColor = preferences["preference_background_past"]
		futureBackgroundColor =
			preferences["preference_background_future"]
		nowLineColor = preferences["preference_marker"]
		horizontalFlingEnabled = preferences["preference_fling_enable"]
		snapToWeek =
			!preferences.get<Boolean>("preference_week_snap_to_days") && numberOfVisibleDays != 1
	}

	private fun <T> WeekView<T>.setupHours() {
		val lines = MutableList(0) { return@MutableList 0 }
		val labels = MutableList(0) { return@MutableList "" }
		val range = RangePreference.convertToPair(
			preferences.get<String>(
				"preference_timetable_range",
				null
			)
		)

		profileUser.timeGrid.days.maxByOrNull { it.units.size }?.units?.forEachIndexed { index, hour ->
			if (range?.let { index < it.first - 1 || index >= it.second } == true) return@forEachIndexed

			val startTime =
				hour.startTime.toLocalTime()
					.toString(DateTimeUtils.shortDisplayableTime())
			val endTime = hour.endTime.toLocalTime()
				.toString(DateTimeUtils.shortDisplayableTime())

			val startTimeParts = startTime.split(":")
			val endTimeParts = endTime.split(":")

			val startTimeInt =
				startTimeParts[0].toInt() * 60 + startTimeParts[1].toInt()
			val endTimeInt =
				endTimeParts[0].toInt() * 60 + endTimeParts[1].toInt()

			lines.add(startTimeInt)
			lines.add(endTimeInt)
			labels.add(hour.label)
		}

		if (!preferences.get<Boolean>("preference_timetable_range_index_reset"))
			hourIndexOffset = (range?.first ?: 1) - 1
		hourLines = lines.toIntArray()
		hourLabels = labels.toTypedArray().let { hourLabelArray ->
			if (hourLabelArray.joinToString("") == "") IntArray(
				labels.size,
				fun(idx: Int): Int { return idx + 1 }).map { it.toString() }
				.toTypedArray()
			else hourLabelArray
		}
		startTime = lines.first()
		endTime =
			lines.last() + 30 // TODO: Don't hard-code this offset
	}
}

private val Int.zeroToNull: Int?
	get() = if (this != 0) this else null
