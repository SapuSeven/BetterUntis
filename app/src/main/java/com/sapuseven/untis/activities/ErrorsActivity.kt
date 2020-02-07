package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ErrorsAdapter
import com.sapuseven.untis.dialogs.ErrorReportingDialog
import kotlinx.android.synthetic.main.activity_errors.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.File

class ErrorsActivity : BaseActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_errors)

		loadErrorList()
	}

	private fun loadErrorList() {
		recyclerview_errors.layoutManager = LinearLayoutManager(this)
		recyclerview_errors.adapter = ErrorsAdapter(File(filesDir, "logs").listFiles()?.let { files ->
			files.sortedDescending()
					.map {
						val timestamp = it.name.replace(Regex("""^(\d+)(-\d+)?.log$"""), "$1").toLongOrNull()

						ErrorData(
								readCrashData(it),
								timestamp?.let { DateTime(timestamp).toString(DateTimeFormat.mediumDateTime()) }
										?: "(unknown date)" // TODO: Extract string resource
						)
					}
					.groupingBy { it }
					.eachCount()
					.map {
						ErrorData(
								it.key.log,
								"${it.key.time} (${it.value})"
						)
					}
		} ?: emptyList())
		(recyclerview_errors.adapter as ErrorsAdapter).setOnItemClickListener { item ->
			ErrorReportingDialog(this).showGenericErrorDialog(item.log)
		}
	}

	data class ErrorData(
			val log: String,
			val time: String
	)
}
