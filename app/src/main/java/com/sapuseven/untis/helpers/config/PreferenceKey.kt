package com.sapuseven.untis.helpers.config

import androidx.datastore.preferences.core.*

fun intPreferenceKey(profile: Long, name: String): Preferences.Key<Int> = intPreferencesKey("${profile}_$name")
fun doublePreferenceKey(profile: Long, name: String): Preferences.Key<Double> = doublePreferencesKey("${profile}_$name")
fun stringPreferenceKey(profile: Long, name: String): Preferences.Key<String> = stringPreferencesKey("${profile}_$name")
fun booleanPreferenceKey(profile: Long, name: String): Preferences.Key<Boolean> = booleanPreferencesKey("${profile}_$name")
fun floatPreferenceKey(profile: Long, name: String): Preferences.Key<Float> = floatPreferencesKey("${profile}_$name")
fun longPreferenceKey(profile: Long, name: String): Preferences.Key<Long> = longPreferencesKey("${profile}_$name")
fun stringSetPreferenceKey(profile: Long, name: String): Preferences.Key<Set<String>> = stringSetPreferencesKey("${profile}_$name")
