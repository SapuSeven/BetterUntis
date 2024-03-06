package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.model.untis.Auth
import io.ktor.http.HttpMethod

/**
 * Defines a config object for a given request.
 * NOTE: This object doesn't include 'body' because it
 *       allows for caching of the constructed object
 *       for many request definitions.
 */
data class RequestConfig(
	val path: String,
	val auth: Auth? = null
)
