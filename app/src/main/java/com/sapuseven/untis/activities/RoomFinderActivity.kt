package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.ui.common.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.theme.AppTheme

class RoomFinderActivity : BaseComposeActivity() {
	/*private var roomListMargins: Int = 0
	private var hourIndex: Int = 0
	private var maxHourIndex = 0*/

	private var api: UntisRequest = UntisRequest()

	private lateinit var userDatabase: UserDatabase
	private lateinit var user: UserDatabase.User
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	companion object {
		const val EXTRA_INT_ROOM_ID = "com.sapuseven.untis.activities.roomid"

		const val EVENT_PICKER_TAG = "com.sapuseven.untis.activities.elementPicker"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		userDatabase = UserDatabase.createInstance(this)
		userDatabase.getUser(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))?.let { user = it }
		// TODO: Move this part to BaseComposeActivity and check if user is set

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user.id!!)

		setContent {
			AppTheme {
				RoomFinder_Main()
			}
		}

		/*supportActionBar?.setDisplayHomeAsUpEnabled(true)

		roomListMargins = (12 * resources.displayMetrics.density + 0.5f).toInt()

		setupNoRoomsIndicator()
		loadDatabases(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))
		setupHourSelector()
		setupRoomList()
		refreshRoomList()

		swiperefreshlayout_roomfinder_roomlist.setOnRefreshListener {
			updateRooms()
		}*/
	}


	@OptIn(ExperimentalMaterial3Api::class)
	@Preview(showBackground = true)
	@Composable
	fun RoomFinder_Main() {
		var showElementPicker by rememberSaveable { mutableStateOf(false) }

		if (showElementPicker)
			ElementPickerDialogFullscreen(
				title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
				multiSelect = true,
				hideTypeSelection = true,
				initialType = TimetableDatabaseInterface.Type.ROOM,
				timetableDatabaseInterface = timetableDatabaseInterface,
				onDismiss = { showElementPicker = false },
				onSelect = {
				}
			)
		else
			Scaffold(
				topBar = {
					CenterAlignedTopAppBar(
						title = {
							Text(stringResource(id = R.string.activity_title_free_rooms))
						},
						navigationIcon = {
							IconButton(onClick = { finish() }) {
								Icon(
									imageVector = Icons.Filled.ArrowBack,
									contentDescription = "TODO"
								)
							}
						},
						actions = {
							IconButton(onClick = { showElementPicker = true }) {
								Icon(
									imageVector = Icons.Filled.Add,
									contentDescription = stringResource(id = R.string.all_add)
								)
							}
						}
					)
				}
			) { innerPadding ->
				Column(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize()
				) {
					val roomList by remember { mutableStateOf(emptyList<RoomStatusData>()) }

					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						LazyColumn(
							Modifier.fillMaxSize()
						) {
							items(roomList) {
								RoomListItem(it)
							}
						}

						Text(
							text = stringResource(R.string.roomfinder_no_rooms),
							modifier = Modifier.align(Alignment.Center)
						)
					}
				}
			}
	}

	@Composable
	fun RoomListItem(item: RoomStatusData) {
		var isLoading by remember { mutableStateOf(true) }
	}

	class RoomStatusData(
		val name: String,
		val id: Int,
		var loading: Boolean,
		var states: List<Boolean> = emptyList()
	) : Comparable<RoomStatusData> {
		//private var startDate: LocalDate = LocalDate.now()
		var hourIndex: Int = 0

		fun getState(): Int {
			if (loading)
				return STATE_LOADING
			var i = 0
			var hours = 0
			while (hourIndex + i < states.size && !states[hourIndex + i]) {
				hours++
				i++
			}
			return hours
		}

		override fun compareTo(other: RoomStatusData): Int {
			val state1 = getState()
			val state2 = other.getState()

			return when {
				state2 > state1 -> 1
				state1 > state2 -> -1
				else -> name.compareTo(other.name)
			}
		}

		override fun hashCode(): Int {
			return name.hashCode()
		}

		override fun equals(other: Any?): Boolean {
			return other is RoomStatusData && other.name == name
		}

		override fun toString(): String {
			return name
		}

		companion object {
			const val STATE_OCCUPIED = 0
			const val STATE_FREE = 1
			const val STATE_LOADING = -1
		}
	}

	/*private fun updateRooms() {
		val roomsToLoad = roomList.map {
			@Suppress("RemoveRedundantQualifierName")
			PeriodElement(TimetableDatabaseInterface.Type.ROOM.toString(), it.id, it.id)
		}
		roomList.clear()
		roomListAdapter.notifyDataSetChanged()
		addRooms(roomsToLoad)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_roomfinder_actions, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.item_roomfinder_add -> {
			showItemList()
			false
		}
		else -> {
			super.onOptionsItemSelected(item)
		}
	}

	private fun loadDatabases(profileId: Long) {
		userDatabase = UserDatabase.createInstance(this)
		profileUser = userDatabase.getUser(profileId)
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser?.id ?: -1)

		roomFinderDatabase = RoomfinderDatabase.createInstance(this, profileId)
	}

	private fun setupHourSelector() {
		maxHourIndex = -1 // maxIndex = -1 + length
		profileUser?.let {
			it.timeGrid.days.forEach { day ->
				maxHourIndex += day.units.size
			}
		}

		button_roomfinder_next.setOnClickListener {
			if (hourIndex < maxHourIndex) {
				hourIndex++
				refreshRoomList()
				refreshHourSelector()
			}
		}

		button_roomfinder_previous.setOnClickListener {
			if (hourIndex > 0) {
				hourIndex--
				refreshRoomList()
				refreshHourSelector()
			}
		}

		textview_roomfinder_currenthour.setOnClickListener {
			hourIndex = calculateCurrentHourIndex()
			refreshRoomList()
			refreshHourSelector()
		}

		hourIndex = calculateCurrentHourIndex()
		refreshHourSelector()
	}

	private fun calculateCurrentHourIndex(): Int {
		profileUser?.let {
			val now = LocalDateTime.now()
			var index = 0

			it.timeGrid.days.forEach { day ->
				val dayDate = DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
					.parseLocalDate(day.day)
				if (dayDate.dayOfWeek == now.dayOfWeek)
					day.units.forEach { unit ->
						if (unit.endTime.toLocalTime().millisOfDay > now.millisOfDay)
							return index
						index++
					}
				else
					index += day.units.size
			}
		}
		return 0
	}

	private fun setupNoRoomsIndicator() {
		val text = textview_roomfinder_roomlistempty.text.toString()
		if (text.contains("+")) {
			val ss = SpannableString(text)
			val img = ContextCompat.getDrawable(this, R.drawable.all_add)
			if (img != null) {
				img.setBounds(0, 0, img.intrinsicWidth, img.intrinsicHeight)
				ss.setSpan(
					ImageSpan(img, ImageSpan.ALIGN_BOTTOM),
					text.indexOf("+"), text.indexOf("+") + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
				)
			}
			textview_roomfinder_roomlistempty.text = ss
		}
	}

	private fun setupRoomList() {
		recyclerview_roomfinder_roomlist.layoutManager = LinearLayoutManager(this)
		roomListAdapter = RoomFinderAdapter(this, this, roomList)
		recyclerview_roomfinder_roomlist.adapter = roomListAdapter

		roomFinderDatabase.getAllRooms().forEach {
			roomList.add(RoomFinderAdapterItem(it.name, it.id, false, it.states))
		}
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {
		// Nothing to do
	}

	override fun onPeriodElementClick(
		fragment: Fragment,
		element: PeriodElement?,
		useOrgId: Boolean
	) {
		// Ignore single clicks, wait for onPositiveButtonClick instead
	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		addRooms(dialog.getSelectedItems())
	}

	override fun onClick(v: View) {
		val intent = Intent()
		intent.putExtra(
			EXTRA_INT_ROOM_ID,
			roomList[recyclerview_roomfinder_roomlist.getChildLayoutPosition(v)].id
		)
		setResult(Activity.RESULT_OK, intent)
		finish()
	}

	override fun onDeleteClick(position: Int) {
		showDeleteItemDialog(position)
	}

	private fun addRooms(rooms: List<PeriodElement>) {
		rooms.forEach { room ->
			val roomName = timetableDatabaseInterface.getShortName(
				room.id,
				TimetableDatabaseInterface.Type.ROOM
			)
			val item = RoomFinderAdapterItem(roomName, room.id, true)
			item.hourIndex = hourIndex

			if (roomList.contains(item)) return

			roomList.add(item)

			profileUser?.let { user ->
				val startDate = UntisDate.fromLocalDate(
					LocalDate.now().withDayOfWeek(
						DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
							.parseDateTime(user.timeGrid.days.first().day).dayOfWeek
					)
				)
				val endDate = UntisDate.fromLocalDate(
					LocalDate.now().withDayOfWeek(
						DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
							.parseDateTime(user.timeGrid.days.last().day).dayOfWeek
					)
				)
				val proxyHost: String = preferences["preference_connectivity_proxy_host", null]

				TimetableLoader(WeakReference(this), object : TimetableDisplay {
					override fun addTimetableItems(
						items: List<TimegridItem>,
						startDate: UntisDate,
						endDate: UntisDate,
						timestamp: Long
					) {
						val states = mutableListOf<Boolean>()

						profileUser?.let {
							it.timeGrid.days.forEach { day ->
								val dayDateTime =
									DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
										.parseDateTime(day.day)

								day.units.forEach { unit ->
									val unitStartDateTime = unit.startTime.toLocalTime()
									val unitEndDateTime = unit.endTime.toLocalTime()

									var isOccupied = false
									items.forEach allItems@{ item ->
										if (item.startDateTime.dayOfWeek == dayDateTime.dayOfWeek)
											if (item.startDateTime.millisOfDay <= unitEndDateTime.millisOfDay
												&& item.endDateTime.millisOfDay >= unitStartDateTime.millisOfDay
											) {
												isOccupied = true
												return@allItems
											}
									}

									states.add(isOccupied)
								}
							}
						}

						item.states = states.toList()
						item.loading = false

						refreshRoomList()

						roomFinderDatabase.addRoom(
							RoomFinderItem(
								room.id,
								roomName,
								item.states
							)
						)
					}

					override fun onTimetableLoadingError(
						requestId: Int,
						code: Int?,
						message: String?
					) {
						roomList.remove(item)
						refreshRoomList()
						Snackbar.make(
							content_roomfinder,
							if (code != null) ErrorMessageDictionary.getErrorMessage(
								resources,
								code
							) else message
								?: getString(R.string.all_error),
							Snackbar.LENGTH_INDEFINITE
						)
							.setAction("Show") {
								ErrorReportingDialog(this@RoomFinderActivity).showRequestErrorDialog(
									requestId,
									code,
									message
								)
							}
							.show()
					}
				}, user, timetableDatabaseInterface)
					.load(
						TimetableLoader.TimetableLoaderTarget(
							startDate,
							endDate,
							room.id,
							room.type
						), TimetableLoader.FLAG_LOAD_SERVER, proxyHost
					)
			}
		}

		refreshRoomList()
	}

	private fun refreshRoomList() {
		/*if (roomList.isNotEmpty())
			textview_roomfinder_roomlistempty.visibility = View.GONE
		else // default to visible if empty
			textview_roomfinder_roomlistempty.visibility = View.VISIBLE*/

		if (roomList.find { it.loading } == null) swiperefreshlayout_roomfinder_roomlist.isRefreshing =
			false

		roomListAdapter.currentHourIndex = hourIndex
		roomList.sort()
		roomListAdapter.notifyDataSetChanged()
	}

	private fun showItemList() {
		ElementPickerDialog.newInstance(
			timetableDatabaseInterface,
			ElementPickerDialog.Companion.ElementPickerDialogConfig(
				TimetableDatabaseInterface.Type.ROOM,
				multiSelect = true,
				hideTypeSelection = true,
				positiveButtonText = getString(R.string.all_add)
			)
		).show(supportFragmentManager, EVENT_PICKER_TAG)
	}

	private fun showDeleteItemDialog(position: Int) {
		MaterialAlertDialogBuilder(this)
			.setTitle(getString(R.string.roomfinder_dialog_itemdelete_title, roomList[position]))
			.setMessage(R.string.roomfinder_dialog_itemdelete_text)
			.setPositiveButton(R.string.all_yes) { _, _ ->
				if (roomFinderDatabase.deleteRoom(roomList[position].id)) {
					roomList.removeAt(position)
					roomListAdapter.notifyItemRemoved(position)
					refreshRoomList()
				}
			}
			.setNegativeButton(R.string.all_no) { dialog, _ -> dialog.dismiss() }
			.show()
	}

	private fun refreshHourSelector() {
		val unit = getUnitFromIndex(hourIndex)
		unit?.let {
			textview_roomfinder_currenthour.text = getString(
				R.string.roomfinder_current_hour,
				translateDay(unit.first.day),
				unit.second
			)
			textview_roomfinder_currenthourtime.text = getString(
				R.string.roomfinder_current_hour_time,
				unit.third.startTime.toLocalTime().toString(DateTimeFormat.shortTime()),
				unit.third.endTime.toLocalTime().toString(DateTimeFormat.shortTime())
			)
		}

		button_roomfinder_previous.isEnabled = hourIndex != 0
		button_roomfinder_next.isEnabled = hourIndex != maxHourIndex
	}

	private fun translateDay(day: String): String {
		return DateTimeFormat.forPattern("EEEE")
			.print(DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(day))
	}

	/**
	 * @return A triple of the day, the unit index of day (1-indexed) and the unit corresponding to the provided hour index.
	 */
	private fun getUnitFromIndex(index: Int): Triple<Day, Int, Unit>? {
		profileUser?.let {
			var indexCounter = index
			it.timeGrid.days.forEach { day ->
				if (indexCounter >= day.units.size)
					indexCounter -= day.units.size
				else
					return Triple(day, indexCounter + 1, day.units[indexCounter])
			}
		}
		return null
	}*/
}
