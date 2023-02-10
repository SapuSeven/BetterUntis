package com.sapuseven.untis.activities

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_ABSENCES
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_OFFICEHOURS
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.RoomFinderDatabase
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.*
import com.sapuseven.untis.models.untis.UntisAttachment
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.InfoCenter
import com.sapuseven.untis.ui.activities.rememberInfoCenterState
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class InfoCenterActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme(navBarInset = false) {
				withUser { user ->
					val state = rememberInfoCenterState(
						userDatabase,
						user,
						timetableDatabaseInterface,
						dataStorePreferences,
						this
					)
					InfoCenter(state)
				}
			}
		}
	}
}
