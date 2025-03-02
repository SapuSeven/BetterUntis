package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.data.model.github.GitHubApi.URL_GITHUB_PRIVACY_POLICY
import com.sapuseven.untis.data.model.github.GitHubApi.URL_GITHUB_REPOSITORY
import com.sapuseven.untis.ui.navigation.AppRoutes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsCategoryAbout(navController: NavController) {
	val uriHandler = LocalUriHandler.current

	Preference(
		title = { Text(stringResource(R.string.app_name)) },
		summary = {
			Text(
				stringResource(
					R.string.preference_info_app_version_desc,
					BuildConfig.VERSION_NAME,
					BuildConfig.VERSION_CODE
				)
			)
		},
		onClick = {
			uriHandler.openUri("$URL_GITHUB_REPOSITORY/releases")
		},
		leadingContent = {
			Icon(
				painter = painterResource(R.drawable.settings_about_app_icon),
				contentDescription = null
			)
		}
	)

	PreferenceGroup(stringResource(id = R.string.preference_info_general)) {
		val externalSourceDialog = remember { mutableStateOf(false) }

		Preference(
			title = { Text(stringResource(R.string.preference_info_github)) },
			summary = {
				//Text(URL_GITHUB_REPOSITORY)
			},
			onClick = {
				uriHandler.openUri(URL_GITHUB_REPOSITORY)
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_info_github),
					contentDescription = null
				)
			}
		)

		Preference(
			title = { Text(stringResource(R.string.preference_info_license)) },
			summary = { Text(stringResource(R.string.preference_info_license_desc)) },
			onClick = {
				uriHandler.openUri("$URL_GITHUB_REPOSITORY/blob/master/LICENSE")
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_info_github),
					contentDescription = null
				)
			}
		)

		Preference(
			title = { Text(stringResource(R.string.preference_info_contributors)) },
			summary = { Text(stringResource(R.string.preference_info_contributors_desc)) },
			onClick = {
				externalSourceDialog.value = true
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_about_contributor),
					contentDescription = null
				)
			}
		)

		if (externalSourceDialog.value) {
			AlertDialog(
				onDismissRequest = {
					externalSourceDialog.value = false
				},
				confirmButton = {
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier
							.fillMaxWidth()
					) {
						TextButton(
							onClick = {
								uriHandler.openUri(URL_GITHUB_PRIVACY_POLICY)
							}
						) {
							Text(text = stringResource(id = R.string.preference_info_privacy_policy))
						}

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							TextButton(
								onClick = {
									externalSourceDialog.value = false
								}
							) {
								Text(text = stringResource(id = R.string.all_cancel))
							}

							TextButton(
								onClick = {
									externalSourceDialog.value = false
									navController.navigate(AppRoutes.Settings.About.Contributors)
								}
							) {
								Text(text = stringResource(id = R.string.all_ok))
							}
						}
					}
				},
				title = {
					Text(text = stringResource(id = R.string.preference_info_privacy))
				},
				text = {
					Text(stringResource(id = R.string.preference_info_privacy_desc))
				}
			)
		}

		Preference(
			title = { Text(stringResource(R.string.preference_info_libraries)) },
			summary = { Text(stringResource(R.string.preference_info_libraries_desc)) },
			onClick = { navController.navigate(AppRoutes.Settings.About.Libraries) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_about_library),
					contentDescription = null
				)
			}
		)
	}
}
