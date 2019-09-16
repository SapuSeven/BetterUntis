package com.sapuseven.untis.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.RoomFinderAdapter
import com.sapuseven.untis.adapters.RoomFinderAdapterItem
import com.sapuseven.untis.data.databases.RoomfinderDatabase
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import com.sapuseven.untis.models.untis.masterdata.timegrid.Unit
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlinx.android.synthetic.main.activity_roomfinder.*
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class RoomFinderActivity : BaseActivity(), ElementPickerDialog.ElementPickerDialogListener, RoomFinderAdapter.RoomFinderClickListener {
	private var roomListMargins: Int = 0
	private var hourIndex: Int = 0
	private var maxHourIndex = 0
	private var roomList: MutableList<RoomFinderAdapterItem> = ArrayList()
	private var roomListAdapter = RoomFinderAdapter(this, this)
	private var profileUser: UserDatabase.User? = null
	private lateinit var userDatabase: UserDatabase
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var roomFinderDatabase: RoomfinderDatabase

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
		const val EXTRA_INT_ROOM_ID = "com.sapuseven.untis.activities.roomid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_roomfinder)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		roomListMargins = (12 * resources.displayMetrics.density + 0.5f).toInt()

		setupNoRoomsIndicator()
		loadDatabases(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))
		setupHourSelector()
		setupRoomList()
		refreshRoomList()
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
				val dayDate = DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseLocalDate(day.day)
				if (dayDate.dayOfWeek == now.dayOfWeek)
					day.units.forEach { unit ->
						val unitEndTime = DateTimeFormat.forPattern("'T'HH:mm").withLocale(Locale.ENGLISH).parseLocalTime(unit.endTime)
						if (unitEndTime.millisOfDay > now.millisOfDay)
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
				ss.setSpan(ImageSpan(img, ImageSpan.ALIGN_BOTTOM),
						text.indexOf("+"), text.indexOf("+") + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
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

	override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?, useOrgId: Boolean) {
		// Ignore single clicks, wait for onPositiveButtonClick instead
	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		addRooms(dialog.getSelectedItems())
	}

	override fun onClick(v: View) {
		val intent = Intent()
		intent.putExtra(EXTRA_INT_ROOM_ID, roomList[recyclerview_roomfinder_roomlist.getChildLayoutPosition(v)].id)
		setResult(Activity.RESULT_OK, intent)
		finish()
	}

	override fun onDeleteClick(position: Int) {
		showDeleteItemDialog(position)
	}

	override fun onExpiredClick(position: Int) {
		// TODO: Refresh item
	}

	private fun addRooms(rooms: List<PeriodElement>) {
		rooms.forEach { room ->
			val roomName = timetableDatabaseInterface.getShortName(room.id, TimetableDatabaseInterface.Type.ROOM)
			val item = RoomFinderAdapterItem(roomName, room.id, true)
			item.hourIndex = hourIndex

			roomList.add(item)

			profileUser?.let { user ->
				val startDate = UntisDate.fromLocalDate(LocalDate.now().withDayOfWeek(
						DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(user.timeGrid.days.first().day).dayOfWeek
				))
				val endDate = UntisDate.fromLocalDate(LocalDate.now().withDayOfWeek(
						DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(user.timeGrid.days.last().day).dayOfWeek
				))

				TimetableLoader(WeakReference(this), object : TimetableDisplay {
					override fun addData(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
						val states = mutableListOf<Boolean>()

						profileUser?.let {
							it.timeGrid.days.forEach { day ->
								val dayDateTime = DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(day.day)

								day.units.forEach { unit ->
									val unitStartDateTime = DateTimeFormat.forPattern("'T'HH:mm").withLocale(Locale.ENGLISH).parseDateTime(unit.startTime)
									val unitEndDateTime = DateTimeFormat.forPattern("'T'HH:mm").withLocale(Locale.ENGLISH).parseDateTime(unit.endTime)

									var isOccupied = false
									items.forEach allItems@{ item ->
										if (item.startDateTime.dayOfWeek == dayDateTime.dayOfWeek)
											if (item.startDateTime.millisOfDay <= unitEndDateTime.millisOfDay
													&& item.endDateTime.millisOfDay >= unitStartDateTime.millisOfDay) {
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

						roomFinderDatabase.addRoom(RoomFinderItem(
								room.id,
								roomName,
								item.states
						))
					}

					override fun onError(requestId: Int, code: Int?, message: String?) {
						roomList.remove(item)
						refreshRoomList()
						Snackbar.make(content_roomfinder, if (code != null) ErrorMessageDictionary.getErrorMessage(resources, code) else message
								?: getString(R.string.error), Snackbar.LENGTH_INDEFINITE)
								.show()
						// TODO: Show a button for more info and possibly bug reports
					}
				}, user, timetableDatabaseInterface).load(TimetableLoader.TimetableLoaderTarget(startDate, endDate, room.id, room.type), TimetableLoader.FLAG_LOAD_SERVER)
			}
		}

		refreshRoomList()
	}

	private fun refreshRoomList() {
		if (roomList.isNotEmpty())
			textview_roomfinder_roomlistempty.visibility = View.GONE
		else // default to visible if empty
			textview_roomfinder_roomlistempty.visibility = View.VISIBLE

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
						positiveButtonText = getString(R.string.add))
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}

	private fun showDeleteItemDialog(position: Int) {
		AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_item_title, roomList[position]))
				.setMessage(R.string.delete_item_text)
				.setPositiveButton(R.string.yes) { _, _ ->
					if (roomFinderDatabase.deleteRoom(roomList[position].id)) {
						roomList.removeAt(position)
						roomListAdapter.notifyItemRemoved(position)
						refreshRoomList()
					}
				}
				.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
				.create()
				.show()
	}

	private fun refreshHourSelector() {
		val unit = getUnitFromIndex(hourIndex)
		unit?.let {
			textview_roomfinder_currenthour.text = getString(R.string.roomfinder_current_hour, translateDay(unit.first.day), unit.second)
			textview_roomfinder_currenthourtime.text = getString(R.string.roomfinder_current_hour_time, unit.third.startTime.substring(1), unit.third.endTime.substring(1))
		}

		button_roomfinder_previous.isEnabled = hourIndex != 0
		button_roomfinder_next.isEnabled = hourIndex != maxHourIndex
	}

	private fun translateDay(day: String): String {
		return DateTimeFormat.forPattern("EEEE").print(DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(day))
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
	}
}
