package com.sapuseven.untis.data.connectivity

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_HOST
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.TextUtils
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

	class UntisRequestQuery {
		var url = ""
		var data: UntisRequestData = UntisRequestData()
		var proxyHost: String? = null

		@Throws(URISyntaxException::class, UnsupportedEncodingException::class)
		internal fun getURI(encoding: String): URI {
			return if (!TextUtils.isNullOrEmpty(data.method))
				URI(proxiedUrl() + "?m=" + URLEncoder.encode(data.method, encoding))
			else
				URI(proxiedUrl())
		}

		private fun proxiedUrl() = if (proxyHost.isNullOrBlank()) url else url.replace(DEFAULT_WEBUNTIS_HOST, proxyHost.toString())
	}

	@Serializable
	class UntisRequestData {
		var id: String = ""
		var jsonrpc: String = "2.0"
		var method: String = ""
		var params: List<@ContextualSerialization BaseParams> = emptyList()
	}
}
