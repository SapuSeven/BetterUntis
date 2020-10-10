package com.sapuseven.untis.data.connectivity

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_HOST
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.untis.params.BaseParams
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder

class UntisRequest {
	suspend fun request(query: UntisRequestQuery): Result<String, FuelError> {
		return query.getURI("utf-8").toString().httpPost()
				.header(mapOf("Content-Type" to "application/json; charset=UTF-8"))
				.body(getJSON().stringify(UntisRequestData.serializer(), query.data))
				.awaitStringResult()
	}

	class UntisRequestQuery(val user: UserDatabase.User? = null, apiUrl: String? = null) {
		var url = apiUrl ?: user?.apiUrl ?: user?.schoolId?.let {
			UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + it
		} ?: ""
		var data: UntisRequestData = UntisRequestData()
		var proxyHost: String? = null

		@Throws(URISyntaxException::class, UnsupportedEncodingException::class)
		internal fun getURI(encoding: String): URI {
			val parameters = listOf(
					"m" to URLEncoder.encode(data.method, encoding),
					"school" to (data.school ?: user?.schoolId)
			).mapNotNull {
				if (it.second?.isNotBlank() == true)
					it.first + "=" + it.second
				else null
			}.joinToString("&")

			return URI(proxiedUrl() + if (parameters.isNotBlank()) "?$parameters" else "")
		}

		private fun proxiedUrl() = if (proxyHost.isNullOrBlank()) url else url.replace(DEFAULT_WEBUNTIS_HOST, proxyHost.toString())
	}

	@Serializable
	class UntisRequestData {
		var id: String = "-1"
		var jsonrpc: String = "2.0"
		var method: String = ""
		var school: String? = null
		var params: List<@ContextualSerialization BaseParams> = emptyList()
	}
}
