package com.sapuseven.untis.activities

import android.content.DialogInterface
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
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.RoomFinderAdapter
import com.sapuseven.untis.adapters.RoomFinderAdapterItem
import com.sapuseven.untis.data.databases.User
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import com.sapuseven.untis.models.untis.masterdata.timegrid.Unit
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlinx.android.synthetic.main.activity_roomfinder.*
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class RoomFinderActivity : BaseActivity(), ElementPickerDialog.ElementPickerDialogListener, RoomFinderAdapter.RoomFinderClickListener {
	private var roomListMargins: Int = 0
	private var hourIndex: Int = 0
	private var maxHourIndex = 0
	//private var dialog: AlertDialog? = null
	private var roomList: MutableList<RoomFinderAdapterItem> = ArrayList()
	private var roomListAdapter = RoomFinderAdapter(this, this)
	private var profileUser: User? = null
	private lateinit var userDatabase: UserDatabase
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "profile_id"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_roomfinder)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		roomListMargins = (12 * resources.displayMetrics.density + 0.5f).toInt()

		setupNoRoomsIndicator()

		loadUserDatabase(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))

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

	private fun loadUserDatabase(profileId: Long) {
		userDatabase = UserDatabase.createInstance(this)
		profileUser = userDatabase.getUser(profileId)
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser?.id ?: -1)
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
				displayCurrentHour()
			}
		}

		button_roomfinder_previous.setOnClickListener {
			if (hourIndex > 0) {
				hourIndex--
				refreshRoomList()
				displayCurrentHour()
			}
		}

		textview_roomfinder_currenthour.setOnClickListener {
			hourIndex = calculateCurrentHourIndex()
			refreshRoomList()
			displayCurrentHour()
		}

		hourIndex = calculateCurrentHourIndex()
		displayCurrentHour()
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
			val img = ContextCompat.getDrawable(this, R.drawable.roomfinder_add)
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

		//reload()
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {

	}

	override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?) {

	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		addRooms(dialog.getSelectedItems())
	}

	override fun onClick(v: View) {
		/*val itemPosition = recyclerview_roomfinder_roomlist.getChildLayoutPosition(v)
		val item = roomList!![itemPosition]

		val intent = Intent()
		val elementName = ElementName(ROOM, userDataList)
		try {
			intent.putExtra("elemId", elementName.findFieldByValue("name", item.getName(), "id") as Int)
		} catch (e: JSONException) {
			e.printStackTrace() // Not expected to occur
		}

		intent.putExtra("elemType", ROOM)
		intent.putExtra("displayName", getString(R.string.title_room, item.getName()))
		setResult(Activity.RESULT_OK, intent)
		finish()*/
	}

	override fun onDeleteClick(position: Int) {
		showDeleteItemDialog(position)
	}

	override fun onExpiredClick(position: Int) {
		//refreshItem(position)
	}

	private fun addRooms(rooms: List<PeriodElement>) {
		rooms.forEach { room ->
			val item = RoomFinderAdapterItem(timetableDatabaseInterface.getShortName(room.id, TimetableDatabaseInterface.Type.ROOM), true)
			item.hourIndex = hourIndex

			roomList.add(item)

			profileUser?.let { user ->
				// TODO: Dynamic week length
				val startDate = UntisDate(LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toString(ISODateTimeFormat.date()))
				val endDate = UntisDate(LocalDate.now().withDayOfWeek(DateTimeConstants.FRIDAY).toString(ISODateTimeFormat.date()))

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

						// TODO: Save the result
						/*val binaryData = StringBuilder()
						for (value in states)
							binaryData.append(if (value) '1' else '0')*/

						/*if (!TextUtils.isEmpty(binaryData.toString())) {
							if (requestModel.isRefreshOnly)
								deleteItem(requestModel.displayName)

							val writer = BufferedWriter(OutputStreamWriter(
									openFileOutput("roomList.txt", Context.MODE_APPEND), "UTF-8"))
							writer.write(requestModel.displayName!!)
							writer.newLine()
							writer.write(binaryData.toString())
							writer.newLine()
							writer.write(String.valueOf(getStartDateFromWeek(Calendar.getInstance(), 0,
									true).getTimeInMillis()))
							writer.newLine()
							writer.close()
						}*/

					}
				}, user, timetableDatabaseInterface).load(startDate, endDate, room.id, room.type)
			}
		}

		refreshRoomList()
	}

	/*private fun reload() {
		roomList!!.clear()
		try {
			val reader = BufferedReader(InputStreamReader(openFileInput("roomList.txt")))
			var name: String
			while ((name = reader.readLine()) != null) {
				val roomItem = AdapterItemRoomFinder(this, name, isInRequestQueue(name))
				val binaryData = reader.readLine()
				val states = BooleanArray(binaryData.length)
				for (i in states.indices)
					states[i] = binaryData[i] == '1'
				roomItem.setStates(states)
				maxHourIndex = states.size - 1
				roomItem.setDate(java.lang.Long.parseLong(reader.readLine()))
				roomList!!.add(roomItem)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}

		for (r in requestQueue!!) {
			if (!r.isRefreshOnly)
				roomList!!.add(AdapterItemRoomFinder(this, r.displayName, true))
			roomAdapter!!.notifyItemInserted(roomList!!.size - 1)
		}

		refreshRoomList()
	}*/

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
		ElementPickerDialog.createInstance(
				timetableDatabaseInterface,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(
						TimetableDatabaseInterface.Type.ROOM,
						multiSelect = true,
						hideTypeSelection = true,
						positiveButtonText = "Add") // TODO: Extract string resource
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}

	private fun showDeleteItemDialog(position: Int) {
		AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_item_title, roomList[position]))
				.setMessage(R.string.delete_item_text)
				.setPositiveButton(R.string.yes) { _, _ ->
					//if (deleteSavedItemData()) { // TODO: Implement if item data is actually stored
					roomList.removeAt(position)
					roomListAdapter.notifyItemRemoved(position)
					refreshRoomList()
					//}
				}
				.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
				.create()
				.show()
	}

	private fun displayCurrentHour() {
		val unit = getUnitFromIndex(hourIndex)
		unit?.let {
			textview_roomfinder_currenthour.text = getString(R.string.roomfinder_current_hour, translateDay(unit.first.day), unit.second)
			textview_roomfinder_currenthourtime.text = getString(R.string.roomfinder_current_hour_time, unit.third.startTime.substring(1), unit.third.endTime.substring(1))
		}
		// TODO: Fallback if unit is null
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

	/*private fun loadRoom(room: RequestModel) {
		val unitManager: TimegridUnitManager
		try {
			unitManager = TimegridUnitManager(MasterData(ListManager.getUserData(application).getJSONObject("masterData")))
		} catch (e: JSONException) {
			e.printStackTrace()
			// TODO: Proper error handling
			return
		}

		val days = unitManager.getNumberOfDays()
		val hours = unitManager.getHoursPerDay()

		val startDateFromWeek = Integer.parseInt(SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
				.format(getStartDateFromWeek(Calendar.getInstance(), 0).getTime()))

		val prefs = getSharedPreferences("loginData", Context.MODE_PRIVATE)
		val sessionInfo = SessionInfo()
		sessionInfo.setElemId(room.roomID)
		sessionInfo.setElemType(getElemTypeName(ROOM))

		val api = UntisRequest(this, sessionInfo)

		val handler = { response ->
			if (response == null) {
				Log.w("ActivityRoomFinder", "response is null")
				// TODO: Stop loading and show "unknown error: null";
				return
			}
			try {
				if (response!!.has("error")) {
					Log.w("error", response!!.toString())
					Snackbar.make(recyclerView,
							getString(R.string.snackbar_error, response!!.getJSONObject("error")
									.getString("message")), Snackbar.LENGTH_LONG)
							.setAction("OK", null).show()
				} else if (response!!.has("result")) {
					val requestModel = requestQueue!![0]

					val timetable = Timetable(response!!.getJSONObject("result"), PreferenceManager.getDefaultSharedPreferences(applicationContext))

					val states = BooleanArray(days * hours)

					for (i in states.indices) {
						val day = i / hours
						val hour = i % hours

						if (timetable.getItems(day, hour).size() > 0)
							states[day * hours + hour] = true
					}

					val binaryData = StringBuilder()
					for (value in states)
						binaryData.append(if (value) '1' else '0')

					if (!TextUtils.isEmpty(binaryData.toString())) {
						if (requestModel.isRefreshOnly)
							deleteItem(requestModel.displayName)

						val writer = BufferedWriter(OutputStreamWriter(
								openFileOutput("roomList.txt", Context.MODE_APPEND), "UTF-8"))
						writer.write(requestModel.displayName!!)
						writer.newLine()
						writer.write(binaryData.toString())
						writer.newLine()
						writer.write(String.valueOf(getStartDateFromWeek(Calendar.getInstance(), 0,
								true).getTimeInMillis()))
						writer.newLine()
						writer.close()
					}

					reload()
					refreshRoomList()
				}
			} catch (e: JSONException) {
				e.printStackTrace()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			requestQueue!!.removeAt(0)
			executeRequestQueue()
		}

		val query = UntisRequest.UntisRequestQuery()
		query.setMethod(Constants.UntisAPI.METHOD_GET_TIMETABLE)
		query.setHost(prefs.getString("url", null))
		query.setPath("/WebUntis/jsonrpc_intern.do")
		query.setSchool(prefs.getString("school", null))

		val params = JSONObject()
		try {
			params
					.put("id", room.roomID)
					.put("type", getElemTypeName(ROOM))
					.put("startDate", startDateFromWeek)
					.put("endDate", addDaysToInt(startDateFromWeek, days))
					.put("masterDataTimestamp", System.currentTimeMillis())
					.put("auth", getAuthObject(prefs.getString("user", ""), prefs.getString("key", "")))
		} catch (e: JSONException) {
			e.printStackTrace() // TODO: Implment proper error handling (search for possible cases first)
		}

		query.setParams(JSONArray().put(params))

		api.setCachingMode(UntisRequest.CachingMode.RETURN_CACHE_LOAD_LIVE)
		api.setResponseHandler(handler).submit(query)
	}

	fun getCurrentHourIndex(): Int {
		if (currentHourIndex >= 0)
			return currentHourIndex + hourIndex

		var index = 0

		var masterData: MasterData? = null
		try {
			masterData = MasterData(ListManager.getUserData(application).getJSONObject("masterData"))
		} catch (e: JSONException) {
			e.printStackTrace()
		}

		val unitManager = TimegridUnitManager(masterData)

		val cNow = Calendar.getInstance()
		val cToCompare = Calendar.getInstance()

		val startDateFromWeek = Integer.parseInt(SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
				.format(DateOperations.getStartDateFromWeek(Calendar.getInstance(), 0).getTime()))

		for (i in 0 until unitManager.getNumberOfDays() * unitManager.getHoursPerDay()) {
			val dateTime = (addDaysToInt(startDateFromWeek, i / unitManager.getHoursPerDay(),
					SimpleDateFormat("yyyy-MM-dd'T'", Locale.ENGLISH))
					+ String.format("%1$5s", unitManager.getUnits().get(i % unitManager
					.getHoursPerDay()).getDisplayEndTime()).replace(' ', '0') + "Z")

			try {
				cToCompare.time = DateOperations.parseFromISO(dateTime)
			} catch (e: ParseException) {
				e.printStackTrace()
			}

			if (cNow.timeInMillis > cToCompare.timeInMillis)
				index++
			else
				break
		}

		if (index == unitManager.getNumberOfDays() * unitManager.getHoursPerDay())
			index = 0

		Log.d("RoomFinder", "Current Hour Index: $index")
		currentHourIndex = Math.max(index, 0)
		return currentHourIndex + hourIndex
	}

	private fun deleteItem(name: String?): Boolean {
		val inputFile = File(filesDir, "roomList.txt")
		val tempFile = File(filesDir, "roomList.txt.tmp")

		val reader = BufferedReader(InputStreamReader(
				openFileInput("roomList.txt"), "UTF-8"))
		val writer = BufferedWriter(OutputStreamWriter(
				openFileOutput("roomList.txt.tmp", Context.MODE_APPEND), "UTF-8"))

		var currentLine: String

		while ((currentLine = reader.readLine()) != null) {
			val trimmedLine = currentLine.trim { it <= ' ' }

			if (trimmedLine == name) {
				reader.readLine()
				reader.readLine()
				continue
			}

			writer.write(currentLine)
			writer.newLine()
			writer.write(reader.readLine())
			writer.newLine()
			writer.write(reader.readLine())
			writer.newLine()
		}
		reader.close()
		writer.close()

		return inputFile.delete() && tempFile.renameTo(inputFile)
	}

	fun refreshItem(position: Int) {
		AlertDialog.Builder(this)
				.setTitle(R.string.refresh_item_title)
				.setMessage(getString(R.string.refresh_item_text))
				.setPositiveButton(R.string.refresh_this_item, { dialog, which ->
					refreshItemData(position)
					roomAdapter!!.notifyDataSetChanged()
					executeRequestQueue()
					dialog.dismiss()
				})
				.setNeutralButton(R.string.refresh_all_items, { dialog, which ->
					for (i in roomList!!.indices)
						if (roomList!![i].isOutdated())
							refreshItemData(i)
					roomAdapter!!.notifyDataSetChanged()
					executeRequestQueue()
					dialog.dismiss()
				})
				.setNegativeButton(R.string.cancel, { dialog, which -> dialog.dismiss() })
				.create()
				.show()
	}

	private fun refreshItemData(position: Int) {
		roomList!![position].setLoading()

		val elementName = ElementName(ROOM, userDataList)

		try {
			requestQueue!!.add(RequestModel(elementName.findFieldByValue("name",
					roomList!![position].getName(), "id") as Int, roomList!![position].getName(), true))
		} catch (e: JSONException) {
			e.printStackTrace() // Not expected to occur
		}

	}

	override fun onClick(v: View) {
		val itemPosition = recyclerView!!.getChildLayoutPosition(v)
		val item = roomList!![itemPosition]

		val intent = Intent()
		val elementName = ElementName(ROOM, userDataList)
		try {
			intent.putExtra("elemId", elementName.findFieldByValue("name", item.getName(), "id") as Int)
		} catch (e: JSONException) {
			e.printStackTrace() // Not expected to occur
		}

		intent.putExtra("elemType", ROOM)
		intent.putExtra("displayName", getString(R.string.title_room, item.getName()))
		setResult(Activity.RESULT_OK, intent)
		finish()
	}*/
}
