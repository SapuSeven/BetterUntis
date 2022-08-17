package com.sapuseven.untis.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.flow.*

class UntisPreferenceDataStore<T>(
	val dataStore: DataStore<Preferences>?,
	val prefKey: Preferences.Key<T>,
	val defaultValue: T,
	val dependencyValue: (prefValue: T) -> Boolean = {
		when (it) {
			is Boolean -> it
			is String -> it.isNotEmpty()
			else -> true
		}
	},
	val subDependency: UntisPreferenceDataStore<*>? = null
) {
	companion object {
		fun emptyDataStore() = UntisPreferenceDataStore(null, booleanPreferencesKey(""), false)
	}

	fun getValueFlow() =
		dataStore?.data?.map { prefs -> prefs[prefKey] ?: defaultValue } ?: emptyFlow()

	/**
	 * Returns a flow of boolean values that determine whether the depending preference should be
	 * enabled or not.
	 *
	 * If this data store has an assigned subDependency, all dependencies will be checked
	 * recursively and this flow only returns `true` if all other dependencies return `true`.
	 */
	fun getDependencyFlow(): Flow<Boolean> {
		val dependencyFlow = dataStore?.data?.map { prefs -> prefs[prefKey] ?: defaultValue }?.map(dependencyValue) ?: flowOf(true)
		return dependencyFlow.combine(subDependency?.getDependencyFlow() ?: flowOf(true)) { a, b -> a && b }
	}

	fun isDefaultEnabled() = dependencyValue(defaultValue)

	suspend fun saveValue(value: T) =
		dataStore?.edit { prefs ->
			prefs[prefKey] = value
		}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
	key: String,
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit) = {},
	navController: NavController
) {
	ListItem(
		headlineText = title,
		leadingContent = icon,
		modifier = Modifier.clickable {
			navController.navigate(key)
		}
	)
}

@Composable
fun PreferenceCategory(
	title: String,
	children: (@Composable () -> Unit) = {}
) {
	Text(
		text = title,
		style = MaterialTheme.typography.labelMedium,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
	)
	Column {
		children()
	}
	Divider(
		color = MaterialTheme.colorScheme.outline,
		modifier = Modifier.padding(vertical = 8.dp)
	)
}
