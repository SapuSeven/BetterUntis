package com.sapuseven.untis.wear.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.input.RotaryEncoder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import androidx.preference.PreferenceManager
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.wear.adapters.TimetableListAdapter
import com.sapuseven.untis.wear.data.TimeGridItem
import com.sapuseven.untis.wear.helpers.TimetableLoader
import com.sapuseven.untis.wear.helpers.TimetableSorting.formatItems
import com.sapuseven.untis.wear.interfaces.TimetableDisplay
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class MainActivity : WearableActivity(), TimetableDisplay {

    private var scrollView: ScrollView? = null
    private var preferences: com.sapuseven.untis.helpers.config.PreferenceManager? = null
    private var timetableListAdapter: TimetableListAdapter? = null
    private val userDatabase = UserDatabase.createInstance(this)
    private var profileId: Long = -1

    private lateinit var profileUser: UserDatabase.User
    private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
    private lateinit var timetableLoader: TimetableLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adjustInset(findViewById(R.id.content))
        scrollView = findViewById(R.id.root)
        preferences = com.sapuseven.untis.helpers.config.PreferenceManager(this)
        timetableListAdapter = TimetableListAdapter(this, findViewById(R.id.timetable))

        loadProfile()
        timetableLoader = TimetableLoader(WeakReference(this), this, profileUser, timetableDatabaseInterface)

        findViewById<Button>(R.id.reload).setOnClickListener {
            timetableListAdapter!!.resetListLoading()
            loadTimetable()
        }

        findViewById<Button>(R.id.sign_out).setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("signed_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTimetable()
    }

    private fun loadProfile(): Boolean {
        if (userDatabase.getUsersCount() < 1)
            return false

        profileId = preferences!!.currentProfileId()
        if (profileId == 0L || userDatabase.getUser(profileId) == null) profileId = userDatabase.getAllUsers()[0].id
                ?: 0 // Fall back to the first user if an invalid user id is saved
        if (profileId == 0L) return false // No user found in database
        profileUser = userDatabase.getUser(profileId) ?: return false

        preferences!!.saveProfileId(profileId)
        timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser.id ?: 0)
        return true
    }

    private fun loadTimetable() {
        val today = UntisDate.fromLocalDate(LocalDate.now())
        timetableLoader.load(TimetableLoader.TimetableLoaderTarget(today, today, profileUser.userData.elemId, profileUser.userData.elemType ?: ""), TimetableLoader.FLAG_LOAD_SERVER)
    }

    override fun addTimetableItems(items: List<TimeGridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
        val formattedItems = formatItems(items)
        val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        var data: PeriodData
        var time: String
        var title: String
        var teacher: String
        var room: String
        var text: String
        timetableListAdapter!!.clearList()
        formattedItems.forEach {
            data = it.periodData
            time = it.startDateTime.toString(fmt) + " - " + it.endDateTime.toString(fmt)
            title = if (data.getShortTitle() == "") resources.getString(R.string.main_untitled) else data.getShortTitle()
            teacher = if (it.contextType == TimetableDatabaseInterface.Type.TEACHER.name) data.getShortClasses() else data.getShortTeachers()
            room = if (it.contextType == TimetableDatabaseInterface.Type.ROOM.name)data.getShortClasses() else it.periodData.getShortRooms()

            text = "$time\n$title"
            if (teacher != "") text += ", $teacher"
            if (room != "") text += ", $room"
            timetableListAdapter!!.addItem(text, data.isCancelled())
        }
    }

    override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
        Log.d("Timetable", message ?: "")
        timetableListAdapter!!.resetListUnavailable()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(event)) {
            val delta = -RotaryEncoder.getRotaryAxisValue(event) * RotaryEncoder.getScaledScrollFactor(this)
            scrollView!!.scrollBy(0, delta.roundToInt())
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    private fun adjustInset(layout: View) {
        if (applicationContext.resources.configuration.isScreenRound) {
            val inset = (FACTOR * Resources.getSystem().displayMetrics.widthPixels).toInt()
            layout.setPadding(inset, inset, inset, inset)
        }
    }

    companion object {
        private const val FACTOR = 0.146467f
    }
}
