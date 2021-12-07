package com.sapuseven.untis.helpers

import android.content.res.Resources
import com.sapuseven.untis.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

private const val INVALID_CREDENTIALS_STRING = "invalid_credentials_string"
private const val ERROR_MESSAGE_GENERIC_STRING = "error_message_generic_string"

class ErrorMessageDictionaryTest {
	@Mock
	private lateinit var mockResources: Resources

	@Test
	fun getErrorMessage_returnsCorrectResource() {
		Mockito.`when`(mockResources.getString(R.string.errormessagedictionary_invalid_credentials)).thenReturn(INVALID_CREDENTIALS_STRING)
		Mockito.`when`(mockResources.getString(R.string.errormessagedictionary_generic)).thenReturn(ERROR_MESSAGE_GENERIC_STRING)

		assertThat(ErrorMessageDictionary.getErrorMessage(mockResources, -8504), `is`(INVALID_CREDENTIALS_STRING))
	}

	@Test
	fun getErrorMessage_returnsFallbackResource() {
		Mockito.`when`(mockResources.getString(R.string.errormessagedictionary_generic)).thenReturn(ERROR_MESSAGE_GENERIC_STRING)

		assertThat(ErrorMessageDictionary.getErrorMessage(mockResources, -1), `is`(ERROR_MESSAGE_GENERIC_STRING))
	}

	@Test
	fun getErrorMessage_returnsCustomFallbackResource() {
		Mockito.`when`(mockResources.getString(R.string.errormessagedictionary_invalid_credentials)).thenReturn(INVALID_CREDENTIALS_STRING)

		assertThat(ErrorMessageDictionary.getErrorMessage(mockResources, -1, mockResources.getString(R.string.errormessagedictionary_invalid_credentials)), `is`(INVALID_CREDENTIALS_STRING))
	}
}
