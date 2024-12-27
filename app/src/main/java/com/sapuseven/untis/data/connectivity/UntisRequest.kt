package com.sapuseven.untis.data.connectivity

import android.net.Uri
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_HOST
import com.sapuseven.untis.data.connectivity.UntisApiConstants.DEFAULT_WEBUNTIS_PATH
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.untis.params.BaseParams
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException


class UntisRequest {
	suspend inline fun <reified T> request(query: UntisRequestQuery): Result<T, FuelError> {
		val breadcrumb = Breadcrumb().apply {
			type = "http"
			category = "fuel"
			level = SentryLevel.INFO
			setData("url", query.url)
			setData("method", "POST")
			setData("untis_method", query.data.method)
		}

		return Fuel.post(query.getUri().toString())
			.header(mapOf("Content-Type" to "application/json; charset=UTF-8"))
			.body(getJSON().encodeToString(query.data))
			.response { _, response, _ ->
				breadcrumb.setData("status_code", response.statusCode)
				breadcrumb.setData("reason", response.responseMessage)
				Sentry.addBreadcrumb(breadcrumb)
			}
			.awaitObjectResult(kotlinxDeserializerOf(getJSON()))
	}

	class UntisRequestQuery(val user: User? = null, apiUrl: String? = null) {
		var url = apiUrl ?: user?.apiUrl ?: user?.schoolId?.let {
			"https://" + DEFAULT_WEBUNTIS_HOST + DEFAULT_WEBUNTIS_PATH + it
		} ?: ""
		var data: UntisRequestData = UntisRequestData()
		var proxyHost: String? = null

		@Throws(URISyntaxException::class, UnsupportedEncodingException::class)
		fun getUri(): Uri {
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
