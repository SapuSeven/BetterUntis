package com.sapuseven.untis.activities

import android.content.DialogInterface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlinx.android.synthetic.main.activity_room_finder.*

class RoomFinderActivity : BaseActivity(), ElementPickerDialog.ElementPickerDialogListener/*, View.OnClickListener*/ {
	private var roomListMargins: Int = 0
	//private var dialog: AlertDialog? = null
	private var roomList: ArrayList</*AdapterItemRoomFinder*/ String>? = null
	//private var roomAdapter: AdapterRoomFinder? = null
	private var currentHourIndex = -1
	private var hourIndexOffset: Int = 0
	private var requestQueue: MutableList<RequestModel> = ArrayList()
	//private var recyclerView: RecyclerView? = null
	//private var currentHour: TextView? = null
	private var maxHourIndex = 0

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "profile_id"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_room_finder)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		roomListMargins = (12 * resources.displayMetrics.density + 0.5f).toInt()

		setupNoRoomsIndicator()
		setupRoomList(recyclerview_roomfinder_roomlist)
		setupFab(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))
		setupHourSelector()
	}

	private fun setupFab(profileId: Long) {
		fab_roomfinder_add.setOnClickListener { showItemList(profileId) }
	}

	private fun setupHourSelector() {
		button_roomfinder_next.setOnClickListener {
			if (currentHourIndex + hourIndexOffset < maxHourIndex) {
				hourIndexOffset++
				refreshRoomList()
			}
		}

		button_roomfinder_previous.setOnClickListener {
			if (currentHourIndex + hourIndexOffset > 0) {
				hourIndexOffset--
				refreshRoomList()
			}
		}

		textview_roomfinder_currenthour.setOnClickListener {
			hourIndexOffset = 0
			refreshRoomList()
		}
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

	private fun setupRoomList(listView: RecyclerView) {
		/*roomList = ArrayList<AdapterItemRoomFinder>()
		requestQueue = ArrayList()

		listView.layoutManager = LinearLayoutManager(this)
		roomAdapter = AdapterRoomFinder(this, roomList)
		listView.adapter = roomAdapter

		reload()*/
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {

	}

	override fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?) {

	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		addRooms(dialog.getSelectedItems())
	}

	fun addRooms(rooms: List<PeriodElement>) {
		TODO("not implemented")
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
		if (roomList?.isEmpty() == false)
			textview_roomfinder_roomlistempty.visibility = View.GONE
		else // default to visible if null or empty
			textview_roomfinder_roomlistempty.visibility = View.VISIBLE

		/*Collections.sort(roomList!!) // TODO: Due to multiple calls of getIndex(), this takes quite a bit of time. Better make this asynchronous and display a loading indicator
		roomAdapter!!.notifyDataSetChanged()*/
		displayCurrentHour()
	}

	private fun showItemList(profileId: Long) {
		val userDatabase = UserDatabase.createInstance(this)
		val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, userDatabase.getUser(profileId)?.id
				?: -1)
		ElementPickerDialog.createInstance(
				timetableDatabaseInterface,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(
						TimetableDatabaseInterface.Type.ROOM,
						multiSelect = true,
						hideTypeSelection = true,
						positiveButtonText = "Add") // TODO: Extract string resource
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}


	/*private fun addRoom(item: AdapterItemRoomFinder, roomID: Int) {
		if (roomList!!.contains(item))
			return

		requestQueue!!.add(RequestModel(roomID, item.getName()))

		refreshRoomList()
	}

	private fun executeRequestQueue() {
		reload()

		if (requestQueue!!.size > 0)
			loadRoom(requestQueue!![0])
	}

	private fun loadRoom(room: RequestModel) {
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
			return currentHourIndex + hourIndexOffset

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
		return currentHourIndex + hourIndexOffset
	}

	fun showDeleteItemDialog(position: Int) {
		AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_item_title, roomList!![position].getName()))
				.setMessage(R.string.delete_item_text)
				.setPositiveButton(R.string.yes, { dialog, which ->
					try {
						if (deleteItem(roomList!![position].getName())) {
							roomList!!.removeAt(position)
							roomAdapter!!.notifyItemRemoved(position)
							refreshRoomList()
						}
					} catch (e: IOException) {
						Snackbar.make(recyclerView, getString(R.string.snackbar_error,
								e.message),
								Snackbar.LENGTH_LONG).setAction("OK", null).show()
					}
				})
				.setNegativeButton(R.string.no, { dialog, which -> dialog.dismiss() })
				.create()
				.show()
	}

	@Throws(IOException::class)
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

	}*/

	private fun displayCurrentHour() {
		when {
			hourIndexOffset < 0 -> textview_roomfinder_currenthour.text = resources.getQuantityString(R.plurals.hour_index_last,
					Math.abs(hourIndexOffset), Math.abs(hourIndexOffset))
			hourIndexOffset > 0 -> textview_roomfinder_currenthour.text = resources.getQuantityString(R.plurals.hour_index_next,
					hourIndexOffset, hourIndexOffset)
			else -> textview_roomfinder_currenthour.text = getString(R.string.hour_index_current)
		}
	}

	/*override fun onClick(v: View) {
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

	private data class RequestModel(
			val displayName: String
	) {
		val roomID: Int = 0
		val isRefreshOnly: Boolean = false
	}

	/*companion object {

		fun getRooms(context: Context, includeDisable: Boolean): ArrayList<String> {
			val roomList = ArrayList<String>()

			if (includeDisable)
				roomList.add(context.getString(R.string.preference_note_disable))

			try {
				val file = context.getFileStreamPath("roomList.txt")
				if (file != null && file.exists()) {
					val reader = BufferedReader(InputStreamReader(context.openFileInput("roomList.txt")))
					var name: String
					while ((name = reader.readLine()) != null) {
						for (i in 0..1)
							reader.readLine()
						roomList.add(name)
					}
				}
			} catch (e: IOException) {
				e.printStackTrace()
			}

			return roomList
		}

		fun getRoomStates(context: Context, name: String): String {
			try {
				val reader = BufferedReader(InputStreamReader(
						context.openFileInput("roomList.txt")))
				var line: String
				while ((line = reader.readLine()) != null) {
					if (line == name)
						return reader.readLine()
				}
			} catch (e: IOException) {
				e.printStackTrace()
			}

			return ""
		}
	}*/
}
