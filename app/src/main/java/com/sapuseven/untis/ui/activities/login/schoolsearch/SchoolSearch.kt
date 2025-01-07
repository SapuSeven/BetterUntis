package com.sapuseven.untis.ui.activities.login.schoolsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.ui.activities.login.LoginViewModel
import kotlinx.serialization.ExperimentalSerializationApi


@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SchoolSearch(
	modifier: Modifier,
	viewModel: SchoolSearchViewModel = hiltViewModel(),
	searchText: String,
	onSchoolSelected: (SchoolInfo) -> Unit
) {
	LaunchedEffect(searchText) {
		viewModel.setSearchText(searchText)

	}

	if (viewModel.schoolSearchItems.isNotEmpty()) LazyColumn(modifier) {
		items(viewModel.schoolSearchItems) {
			ListItem(
				headlineContent = { Text(it.displayName) },
				supportingContent = { Text(it.address) },
				modifier = Modifier.clickable { onSchoolSelected(it) }
			)
		}
	}
	else Column(
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
	) {
		if (viewModel.schoolSearchLoading) CircularProgressIndicator()
		else if (viewModel.schoolSearchError != null) Text(text = stringResource(id = viewModel.schoolSearchError!!))
		else if (viewModel.schoolSearchErrorRaw != null) Text(text = viewModel.schoolSearchErrorRaw!!)
		else if (viewModel.schoolSearchItems.isEmpty()) Text(text = stringResource(id = R.string.login_no_results))
	}
}
