package com.sapuseven.untis.helpers

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TextUtilsTest {
	@Test
	fun isNullOrEmpty_returnsTrueIfNull() {
		assertThat(TextUtils.isNullOrEmpty(null), `is`(true))
	}

	@Test
	fun isNullOrEmpty_returnsTrueIfEmpty() {
		assertThat(TextUtils.isNullOrEmpty(""), `is`(true))
	}

	@Test
	fun isNullOrEmpty_returnsFalseIfValid() {
		assertThat(TextUtils.isNullOrEmpty("test"), `is`(false))
	}
}