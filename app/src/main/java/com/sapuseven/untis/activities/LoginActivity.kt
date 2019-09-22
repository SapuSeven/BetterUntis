package com.sapuseven.untis.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.SchoolSearchAdapter
import com.sapuseven.untis.adapters.SchoolSearchAdapterItem
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.SCHOOL_SEARCH_URL
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import com.sapuseven.untis.models.untis.response.SchoolSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity(), View.OnClickListener {
	companion object {
		const val REQUEST_SCAN_CODE = 0x00000001
		const val REQUEST_LOGIN = 0x00000002
	}

	private var searchMode: Boolean = false

	private var tvWelcome: TextView? = null
	private var tvWelcomeMessage: TextView? = null
	private var tvSearchMessage: TextView? = null
	private var pbSearchLoading: ProgressBar? = null
	private var btnScanCode: Button? = null
	private var btnManualDataInput: Button? = null
	private var etSearch: com.google.android.material.textfield.TextInputEditText? = null
	private var rvSearchResults: RecyclerView? = null

	private var layoutManager: RecyclerView.LayoutManager? = null
	private var adapter: SchoolSearchAdapter? = null

	private var api: UntisRequest = UntisRequest()
	private var query: UntisRequest.UntisRequestQuery = UntisRequest.UntisRequestQuery()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		tvWelcome = findViewById(R.id.textview_login_welcome)
		tvWelcomeMessage = findViewById(R.id.textview_login_please_log_in)
		tvSearchMessage = findViewById(R.id.textview_login_search_message)
		pbSearchLoading = findViewById(R.id.progressbar_login_search_loading)
		btnScanCode = findViewById(R.id.button_login_scan_code)
		btnManualDataInput = findViewById(R.id.button_login_manual_data_input)
		etSearch = findViewById(R.id.edittext_login_search)
		rvSearchResults = findViewById(R.id.recyclerview_login_search_results)

		findViewById<Button>(R.id.button_login_scan_code).setOnClickListener {
			try {
				val intent = Intent("com.google.zxing.client.android.SCAN")
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
				startActivityForResult(intent, REQUEST_SCAN_CODE)
			} catch (e: ActivityNotFoundException) {
				try {
					val i = Intent(Intent.ACTION_VIEW)
					i.data = Uri.parse("market://details?id=com.google.zxing.client.android")
					startActivity(i)
				} catch (e2: ActivityNotFoundException) {
					val i = Intent(Intent.ACTION_VIEW)
					i.data = Uri.parse("https://sapuseven.com/scan_qr.php")
					startActivity(i)
				}
			} catch (e: SecurityException) {
				try {
					val i = Intent(Intent.ACTION_VIEW)
					i.data = Uri.parse("market://details?id=com.google.zxing.client.android")
					startActivity(i)
				} catch (e2: ActivityNotFoundException) {
					val i = Intent(Intent.ACTION_VIEW)
					i.data = Uri.parse("https://sapuseven.com/scan_qr.php")
					startActivity(i)
				}
			}
		}

		findViewById<Button>(R.id.button_login_manual_data_input).setOnClickListener {
			val loginDataInputIntent = Intent(this, LoginDataInputActivity::class.java)
			startActivityForResult(loginDataInputIntent, REQUEST_LOGIN)
		}


		etSearch?.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) enableSearchMode(true)
		}

		etSearch?.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

			override fun afterTextChanged(s: Editable) {
				loadResults(s.toString())
			}
		})

		rvSearchResults?.setHasFixedSize(true)

		layoutManager = LinearLayoutManager(this)
		rvSearchResults?.layoutManager = layoutManager

		adapter = SchoolSearchAdapter(this)
		rvSearchResults?.adapter = adapter
	}

	private fun loadResults(search: String) = GlobalScope.launch(Dispatchers.Main) {
		if (!searchMode)
			return@launch

		var untisResponse = SchoolSearchResponse()

		pbSearchLoading?.visibility = View.VISIBLE
		tvSearchMessage?.visibility = View.GONE
		rvSearchResults?.background?.alpha = 128

		query.data.method = UntisApiConstants.METHOD_SEARCH_SCHOOLS
		query.url = SCHOOL_SEARCH_URL
		query.data.params = listOf(SchoolSearchParams(search))

		val result = api.request(query)
		result.fold({ data ->
			untisResponse = getJSON().parse(SchoolSearchResponse.serializer(), data)
		}, { error ->
			println("An error of type ${error.exception} happened: ${error.message}") // TODO: Implement proper error handling
		})

		if (untisResponse.result != null) {
			untisResponse.result?.let { showSchools(it.schools) }
		} else {
			showError(ErrorMessageDictionary.getErrorMessage(resources,
					untisResponse.error?.code,
					untisResponse.error?.message.orEmpty()))
		}

		pbSearchLoading?.visibility = View.GONE
		tvSearchMessage?.visibility = View.VISIBLE
		rvSearchResults?.background?.alpha = 255
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)

		when (requestCode) {
			REQUEST_SCAN_CODE -> if (resultCode == Activity.RESULT_OK) {
				val i = Intent(this, LoginDataInputActivity::class.java)
				data?.let { i.data = Uri.parse(data.getStringExtra("SCAN_RESULT")) }
				startActivityForResult(i, REQUEST_LOGIN)
			}
			REQUEST_LOGIN -> {
				val i = Intent(this, MainActivity::class.java)
				startActivity(i)
				finish()
			}
		}
	}

	override fun onClick(view: View?) {
		view?.let {
			val itemPosition = rvSearchResults?.getChildLayoutPosition(view)

			val schoolInfo = itemPosition?.let { pos -> adapter?.getDatasetItem(pos)?.untisSchoolInfo }

			schoolInfo?.let {
				val builder = Uri.Builder().appendQueryParameter("schoolInfo", getJSON().stringify(UntisSchoolInfo.serializer(), schoolInfo))

				val intent = Intent(this, LoginDataInputActivity::class.java)
				intent.data = builder.build()
				startActivityForResult(intent, REQUEST_LOGIN)
			}
			return@let
		}
	}

	override fun onBackPressed() {
		if (searchMode)
			enableSearchMode(false)
		else
			super.onBackPressed()
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		android.R.id.home -> {
			enableSearchMode(false)
			false
		}
		else -> {
			super.onOptionsItemSelected(item)
		}
	}

	private fun enableSearchMode(enable: Boolean) {
		searchMode = enable

		if (enable) {
			tvWelcome?.visibility = View.GONE
			tvWelcomeMessage?.visibility = View.GONE
			btnScanCode?.visibility = View.GONE
			btnManualDataInput?.visibility = View.GONE
			rvSearchResults?.visibility = View.VISIBLE
			tvSearchMessage?.visibility = View.VISIBLE
			pbSearchLoading?.visibility = View.GONE

			etSearch?.requestFocus()
		} else {
			tvWelcome?.visibility = View.VISIBLE
			tvWelcomeMessage?.visibility = View.VISIBLE
			btnScanCode?.visibility = View.VISIBLE
			btnManualDataInput?.visibility = View.VISIBLE
			rvSearchResults?.visibility = View.GONE
			tvSearchMessage?.visibility = View.GONE
			pbSearchLoading?.visibility = View.GONE

			etSearch?.setText("")
			etSearch?.clearFocus()
			etSearch?.let { hideDefaultKeyboard(it) }
		}

		supportActionBar?.setDisplayHomeAsUpEnabled(enable)
	}

	private fun hideDefaultKeyboard(editText: EditText) {
		val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(editText.windowToken, 0)
	}


	private fun showError(message: String) {
		adapter?.clearDataset()
		adapter?.notifyDataSetChanged()

		tvSearchMessage?.text = message
	}

	private fun showSchools(schools: List<UntisSchoolInfo>) {
		adapter?.clearDataset()

		if (schools.isNotEmpty()) {
			schools.forEach { school: UntisSchoolInfo ->
				adapter?.addToDataset(SchoolSearchAdapterItem(school))
			}

			tvSearchMessage?.text = ""
		} else {
			tvSearchMessage?.text = resources.getString(R.string.login_no_results)
		}

		adapter?.notifyDataSetChanged()
	}
}
