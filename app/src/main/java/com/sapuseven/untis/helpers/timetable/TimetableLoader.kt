package com.sapuseven.untis.helpers.timetable

import android.content.Context
import android.util.Log
import com.sapuseven.untis.api.serializer.Date
import com.sapuseven.untis.data.connectivity.Authentication
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.models.untis.params.TimetableParams
import com.sapuseven.untis.models.untis.response.TimetableResponse
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.joda.time.Instant
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TimetableLoader(
	private val context: WeakReference<Context>,
	private val user: User,
	private val timetableDatabaseInterface: TimetableDatabaseInterface
) {
	companion object {
		const val CODE_REQUEST_FAILED = 2
		const val CODE_REQUEST_PARSING_EXCEPTION = 3
		const val MAX_RETRY_COUNT = 3
		const val RETRY_DELAY_MS = 500L
	}

	private val requestList = ArrayList<TimetableLoaderTarget>()

	private var api: UntisRequest = UntisRequest()
	private var query: UntisRequest.UntisRequestQuery = UntisRequest.UntisRequestQuery(user)

	/*@OptIn(DelicateCoroutinesApi::class)
	fun load(target: TimetableLoaderTarget, flags: Int = 0, proxyHost: String? = null) =
		GlobalScope.launch(Dispatchers.IO) {
			loadAsync(target, flags, proxyHost)
		}*/

	class TimetableLoaderException(
		val requestId: Int,
		val untisErrorCode: Int?,
		val untisErrorMessage: String?
	) : Exception(untisErrorMessage)

	suspend fun loadAsync(
		target: TimetableLoaderTarget,
		proxyHost: String? = null,
		loadFromServer: Boolean = false,
		loadFromCache: Boolean = false,
		loadFromCacheOnly: Boolean = false,
		//onItemsReceived: (timetableItems: TimetableItems) -> Unit
	) {
		/*requestList.add(target)

		var shouldLoadFromServer = loadFromServer

		if (loadFromCache)
			loadFromCache(target, requestList.size - 1)?.let {
				onItemsReceived(it)
			} ?: run {
				shouldLoadFromServer = !loadFromCacheOnly // fall back to server loading
			}

		if (shouldLoadFromServer) {
			for (i in 1..MAX_RETRY_COUNT) {
				try {
					loadFromServer(target, requestList.size - 1, proxyHost, onItemsReceived)
					break
				} catch (e: TimetableLoaderException) {
					if (i < MAX_RETRY_COUNT) {
						Log.d(
							"TimetableLoaderDebug",
							"target $target (requestId ${requestList.size - 1}): Attempt $i unsuccessful, retrying"
						)
						Thread.sleep(RETRY_DELAY_MS)
					} else {
						Log.d(
							"TimetableLoaderDebug",
							"target $target (requestId ${requestList.size - 1}): Attempt $i unsuccessful, max retry limit reached"
						)
						throw e
					}
				}
			}
		}*/
	}

	/*fun loadFlow(
		target: TimetableLoaderTarget,
		proxyHost: String? = null,
		loadFromServer: Boolean = false,
		loadFromCache: Boolean = false,
		loadFromCacheOnly: Boolean = false
	): Flow<TimetableItems> = flow {
		requestList.add(target)

		var shouldLoadFromServer = loadFromServer

		if (loadFromCache)
			loadFromCache(target, requestList.size - 1)?.let {
				emit(it)
			} ?: run {
				shouldLoadFromServer = !loadFromCacheOnly // fall back to server loading
			}

		if (shouldLoadFromServer)
			loadFromServerResult(target, requestList.size - 1, proxyHost).fold({
				emit(it)
			}, {
				// TODO: Show error
			})
	}

	private fun loadFromCache(
		target: TimetableLoaderTarget,
		requestId: Int
	): TimetableItems? {
		val cache = TimetableCache(context)
		cache.setTarget(target.startDate, target.endDate, target.id, target.type, user.id)

		return if (cache.exists()) {
			Log.d(
				"TimetableLoaderDebug",
				"target $target (requestId $requestId): cached file found"
			)
			sendBreadcrumb(target, "cache hit", cache = cache)

			cache.load()?.let { cacheObject ->
				TimetableItems(
					items = cacheObject.items.map {
						periodToTimegridItem(
							it,
							target.type
						)
					},
					startDate = target.startDate,
					endDate = target.endDate,
					timestamp = cacheObject.timestamp
				)
			} ?: run {
				cache.delete()
				Log.d(
					"TimetableLoaderDebug",
					"target $target (requestId $requestId): cached file corrupted"
				)
				sendBreadcrumb(target, "cache corrupted", cache = cache)
				null
			}
		} else {
			Log.d(
				"TimetableLoaderDebug",
				"target $target (requestId $requestId): cached file missing"
			)
			sendBreadcrumb(target, "cache miss", cache = cache)
			null
		}
	}

	private suspend fun loadFromServer(
		target: TimetableLoaderTarget,
		requestId: Int,
		proxyHost: String? = null,
		onItemsReceived: (timetableItems: TimetableItems) -> Unit
	) {
		val cache = TimetableCache(context)
		cache.setTarget(target.startDate, target.endDate, target.id, target.type, user.id)

		query.proxyHost = proxyHost

		val params = TimetableParams(
			target.id,
			target.type,
			target.startDate,
			target.endDate,
			user.masterDataTimestamp,
			0, // TODO: Figure out how timetableTimestamp works
			emptyList(),
			if (user.anonymous) Authentication.createAuthObject() else Authentication.createAuthObject(
				user.user,
				user.key
			)
		)

		query.data.id = requestId.toString()
		query.data.method = UntisApiConstants.METHOD_GET_TIMETABLE
		query.data.params = listOf(params)

		api.request<TimetableResponse>(query).fold({ untisResponse ->
			if (untisResponse.result != null) {
				Log.d(
					"TimetableLoaderDebug",
					"target $target (requestId $requestId): network request success, returning"
				)
				sendBreadcrumb(target, "network success")

				val items = untisResponse.result.timetable.periods
				val timestamp = Instant.now().millis
				onItemsReceived(
					TimetableItems(
						items = items.map {
							periodToTimegridItem(
								it,
								target.type
							)
						},
						startDate = target.startDate,
						endDate = target.endDate,
						timestamp = timestamp
					)
				)
				Log.d(
					"TimetableLoaderDebug",
					"target $target (requestId $requestId): saving to cache: $cache"
				)
				cache.save(TimetableCache.CacheObject(timestamp, items))
				sendBreadcrumb(target, "cache save", cache = cache)

				// TODO: Interpret masterData in the response
			} else {
				val e = TimetableLoaderException(
					requestId,
					untisResponse.error?.code,
					untisResponse.error?.message
				)
				Log.d(
					"TimetableLoaderDebug",
					"target $target (requestId $requestId): network request failed at Untis API level",
					e
				)
				sendBreadcrumb(target, "network failure api", throwable = e)
				throw e
			}
		}, { error ->
			val e = TimetableLoaderException(
				requestId,
				CODE_REQUEST_FAILED,
				error.message
			)
			Log.d(
				"TimetableLoaderDebug",
				"target $target (requestId $requestId): network request failed at OS level",
				e
			)
			sendBreadcrumb(target, "network failure local", throwable = e)
			throw e
		})
	}

	private suspend fun loadFromServerResult(
		target: TimetableLoaderTarget,
		requestId: Int,
		proxyHost: String? = null
	): Result<TimetableItems> {
		val cache = TimetableCache(context)
		cache.setTarget(target.startDate, target.endDate, target.id, target.type, user.id)

		query.proxyHost = proxyHost

		val params = TimetableParams(
			target.id,
			target.type,
			target.startDate,
			target.endDate,
			user.masterDataTimestamp,
			0, // TODO: Figure out how timetableTimestamp works
			emptyList(),
			if (user.anonymous) Authentication.createAuthObject() else Authentication.createAuthObject(
				user.user,
				user.key
			)
		)

		query.data.id = requestId.toString()
		query.data.method = UntisApiConstants.METHOD_GET_TIMETABLE
		query.data.params = listOf(params)

		val request = api.request<TimetableResponse>(query)

		return suspendCoroutine { cont ->
			request.fold({ untisResponse ->
				if (untisResponse.result != null) {
					Log.d(
						"TimetableLoaderDebug",
						"target $target (requestId $requestId): network request success, returning"
					)
					sendBreadcrumb(target, "network success")

					val items = untisResponse.result.timetable.periods
					val timestamp = Instant.now().millis

					Log.d(
						"TimetableLoaderDebug",
						"target $target (requestId $requestId): saving to cache: $cache"
					)
					cache.save(TimetableCache.CacheObject(timestamp, items))
					sendBreadcrumb(target, "cache save", cache = cache)

					cont.resume(
						Result.success(
							TimetableItems(
								items = items.map {
									periodToTimegridItem(
										it,
										target.type
									)
								},
								startDate = target.startDate,
								endDate = target.endDate,
								timestamp = timestamp
							)
						)
					)
					// TODO: Interpret masterData in the response
				} else {
					val e = TimetableLoaderException(
						requestId,
						CODE_REQUEST_FAILED,
						untisResponse.error?.message
					)
					Log.d(
						"TimetableLoaderDebug",
						"target $target (requestId $requestId): network request failed at Untis API level",
						e
					)
					sendBreadcrumb(
						target,
						"network failure api",
						throwable = e
					)
					cont.resume(
						Result.failure(
							TimetableLoaderException(
								requestId,
								untisResponse.error?.code,
								untisResponse.error?.message
							)
						)
					)
				}
			}, { error ->
				val e = TimetableLoaderException(
					requestId,
					CODE_REQUEST_FAILED,
					error.message
				)
				Log.d(
					"TimetableLoaderDebug",
					"target $target (requestId $requestId): network request failed at OS level",
					e
				)
				sendBreadcrumb(target, "network failure local", throwable = e)
				cont.resume(
					Result.failure(
						TimetableLoaderException(
							requestId,
							CODE_REQUEST_FAILED,
							error.message
						)
					)
				)
			})
		}
	}

	private fun formatJsonParsingException(e: JsonDecodingException, jsonData: String): String {
		val errorMargin = 20
		val errorIndex: Int? = e.message?.let {
			it.split(" ")[3].let { i ->
				i.substring(0, i.length - 1)
			}.toIntOrNull()
		}

		return e.toString() + if (errorIndex != null)
			"\n(near " + jsonData.substring((errorIndex - errorMargin).coerceAtLeast(0), (errorIndex + errorMargin).coerceAtMost(jsonData.length)) + ")"
		else ""
	}*/

	/*fun repeat(requestId: Int, flags: Int = 0, proxyHost: String? = null) {
		Log.d(
			"TimetableLoaderDebug",
			"target ${requestList[requestId]} (requestId $requestId): repeat"
		)
		load(requestList[requestId], flags, proxyHost)
	}*/

	private fun sendBreadcrumb(
		target: TimetableLoaderTarget,
		status: String,
		cache: TimetableCache? = null,
		throwable: Throwable? = null
	) {
		Breadcrumb().apply {
			type = "query"
			category = "timetable"
			level = SentryLevel.INFO
			setData("target", target)
			setData("status", status)
			cache?.let { setData("cache_id", cache.toString()) }
			throwable?.let { setData("throwable", throwable) }
			Sentry.addBreadcrumb(this)
		}
	}

	data class TimetableLoaderTarget(
		val startDate: Date,
		val endDate: Date,
		val id: Int,
		val type: String
	)
}
