package com.sapuseven.untis.data.connectivity

import android.net.Uri
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_HOST
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.untis.params.BaseParams
import io.sentry.Breadcrumb
import io.sentry.Breadcrumb.transaction
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SpanStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException


class UntisRequest {
	private fun requestSynchronously(query: UntisRequestQuery): Request {
		FuelManager.instance.client
		return query.getUri().toString().httpPost()
			.header(mapOf("Content-Type" to "application/json; charset=UTF-8"))
			.body(getJSON().encodeToString(query.data))
	}

	suspend fun request(query: UntisRequestQuery): Result<String, FuelError> {
		val breadcrumb = Breadcrumb().apply {
			type = "http"
			category = "fuel"
			level = SentryLevel.INFO
			setData("url", query.url)
			setData("method", "POST")
			setData("untis_method", query.data.method)
		}

		return requestSynchronously(query).response { _, response, _ ->
			breadcrumb.setData("status_code", response.statusCode)
			breadcrumb.setData("reason", response.responseMessage)
			Sentry.addBreadcrumb(breadcrumb)
		}.awaitStringResult()
	}

	class UntisRequestQuery(val user: User? = null, apiUrl: String? = null) {
		var url = apiUrl ?: user?.apiUrl ?: user?.schoolId?.let {
			UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + it
		} ?: ""
		var data: UntisRequestData = UntisRequestData()
		var proxyHost: String? = null

		@Throws(URISyntaxException::class, UnsupportedEncodingException::class)
		internal fun getUri(): Uri {
			return Uri.parse(url).buildUpon().apply {
				if (!proxyHost.isNullOrBlank())
					authority(proxyHost)

				//appendQueryParameter("m", data.method) // optional
				appendQueryParameter("v", "a5.2.3") // required, value taken from Untis Mobile
				//appendQueryParameter("anonymous", "true") // optional
				//appendQueryParameter("server", "euterpe.webuntis.com") // optional
			}.build()
		}
	}

	@Serializable
	class UntisRequestData {
		var id: String = "-1"
		var jsonrpc: String = "2.0"
		var method: String = ""
		var params: List<BaseParams> = emptyList()
	}
}
