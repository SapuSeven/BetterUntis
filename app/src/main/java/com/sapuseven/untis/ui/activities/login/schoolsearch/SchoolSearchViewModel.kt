package com.sapuseven.untis.ui.activities.login.schoolsearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.ui.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SchoolSearchViewModel @Inject constructor(
	val schoolSearchApi: SchoolSearchApi,
) : ViewModel() {
	private val _schoolSearchText = MutableStateFlow<String>("")
	val schoolSearchText: StateFlow<String> = _schoolSearchText

	var schoolSearchItems by mutableStateOf<List<SchoolInfo>>(emptyList())
		private set

	var schoolSearchError: Int? by mutableStateOf(null)
		private set

	var schoolSearchErrorRaw: String? by mutableStateOf(null)
		private set

	var schoolSearchLoading by mutableStateOf(false)
		private set

	init {
		viewModelScope.launch {
			_schoolSearchText
				.debounce(300)
				.distinctUntilChanged()
				.collect { input ->
					searchSchools(input)
				}
		}
	}

	suspend fun searchSchools(input: String) {
		if (input.isEmpty()) return

		schoolSearchError = null
		schoolSearchErrorRaw = null
		schoolSearchItems = emptyList()

		schoolSearchLoading = true
		try {
			schoolSearchItems = schoolSearchApi.searchSchools(search = input).schools
		} catch (e: UntisApiException) {
			schoolSearchError =
				ErrorMessageDictionary.getErrorMessageResource(e.error?.code, false)
			schoolSearchErrorRaw = e.message.orEmpty()
		} catch (e: Exception) {
			schoolSearchError = null
			schoolSearchErrorRaw = e.message.orEmpty()
		} finally {
			schoolSearchLoading = false
		}
	}

	fun setSearchText(searchText: String) {
		_schoolSearchText.value = searchText
	}
}
