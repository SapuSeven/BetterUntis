package com.sapuseven.untis.helpers


fun <K, V> Map<K, V>.ownGetOrDefault(key: K, defaultValue: V): V {
	var v: V
	return if (this[key].also { v = it!! } != null || this.containsKey(key)) {
		v
	} else {
		defaultValue
	}
}

