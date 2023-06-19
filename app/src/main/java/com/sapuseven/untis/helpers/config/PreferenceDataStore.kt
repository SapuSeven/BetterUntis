package com.sapuseven.untis.helpers.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.preferences.UntisPreferenceDataStore

val Context.preferenceDataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
	name = "preferences"
)
val Context.globalDataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
	name = "global"
)

fun Context.intDataStore(
	userId: Long,
	key: String,
	defaultValue: Int = resources.getInteger(
		resources.getIdentifier(
			"${key}_default",
			"integer",
			packageName
		)
	),
	dependencyValue: (prefValue: Int) -> Boolean = { it != 0 },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Int> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = intPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.doubleDataStore(
	userId: Long,
	key: String,
	defaultValue: Double = resources.getInteger(
		resources.getIdentifier(
			"${key}_default",
			"integer",
			packageName
		)
	).toDouble(),
	dependencyValue: (prefValue: Double) -> Boolean = { it != 0.0 },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Double> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = doublePreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.stringDataStore(
	userId: Long,
	key: String,
	defaultValue: String = resources.getString(
		resources.getIdentifier(
			"${key}_default",
			"string",
			packageName
		)
	),
	dependencyValue: (prefValue: String) -> Boolean = { it.isNotBlank() },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<String> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = stringPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.booleanDataStore(
	userId: Long,
	key: String,
	defaultValue: Boolean = resources.getBoolean(
		resources.getIdentifier(
			"${key}_default",
			"bool",
			packageName
		)
	),
	dependencyValue: (prefValue: Boolean) -> Boolean = { it },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Boolean> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = booleanPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.floatDataStore(
	userId: Long,
	key: String,
	defaultValue: Float = resources.getInteger(
		resources.getIdentifier(
			"${key}_default",
			"integer",
			packageName
		)
	).toFloat(),
	dependencyValue: (prefValue: Float) -> Boolean = { it != 0f },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Float> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = floatPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.longDataStore(
	userId: Long,
	key: String,
	defaultValue: Long = resources.getInteger(
		resources.getIdentifier(
			"${key}_default",
			"integer",
			packageName
		)
	).toLong(),
	dependencyValue: (prefValue: Long) -> Boolean = { it != 0L },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Long> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = longPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

fun Context.stringSetDataStore(
	userId: Long,
	key: String,
	defaultValue: Set<String> = emptySet(),
	dependencyValue: (prefValue: Set<String>) -> Boolean = { it.isNotEmpty() },
	subDependency: UntisPreferenceDataStore<*>? = null
): UntisPreferenceDataStore<Set<String>> {
	return UntisPreferenceDataStore(
		dataStore = preferenceDataStore,
		prefKey = stringSetPreferencesKey("${userId}_$key"),
		defaultValue = defaultValue,
		dependencyValue = dependencyValue,
		subDependency = subDependency
	)
}

@Deprecated("This needs to be removed eventually")
suspend fun BaseComposeActivity.deleteProfile(id: Long) {
	preferenceDataStore.edit { prefs ->
		prefs.asMap().keys.filter { it.name.startsWith("${id}_") }.forEach {
			prefs.remove(it)
		}
	}
}
