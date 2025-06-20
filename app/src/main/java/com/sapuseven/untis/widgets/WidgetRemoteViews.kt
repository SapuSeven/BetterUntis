package com.sapuseven.untis.widgets


/*class WidgetRemoteViewsFactory(private val applicationContext: Context, intent: Intent) : RemoteViewsFactory {
	companion object {
		const val EXTRA_INT_WIDGET_ID = "com.sapuseven.widgets.id"
		const val EXTRA_INT_WIDGET_TYPE = "com.sapuseven.widgets.type"

		const val WIDGET_TYPE_UNKNOWN = 0
		const val WIDGET_TYPE_MESSAGES = 1
		const val WIDGET_TYPE_TIMETABLE = 2

		const val STATUS_UNKNOWN = 0
		const val STATUS_DONE = 1
		const val STATUS_LOADING = 2
		const val STATUS_ERROR = 3
	}

	private val appWidgetId = intent.getIntExtra(EXTRA_INT_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
	private val userDatabase = UserDatabase.createInstance(applicationContext)
	private val user = userDatabase.getUser(loadIdPref(applicationContext, appWidgetId))
	private val type = intent.getIntExtra(EXTRA_INT_WIDGET_TYPE, WIDGET_TYPE_UNKNOWN)
	private var items: List<WidgetListItem>? = null

	private var status = STATUS_UNKNOWN

	private val errorItem = WidgetListItem(0, "Failed to load data", "Tap to retry") // TODO: Extract string resources
	private val noLessonsItem = WidgetListItem(0, "No lessons today", "Enjoy your free time!") // TODO: Extract string resources

	private fun loadItems() {
		status = STATUS_LOADING
		try {
			items = when (type) {
				WIDGET_TYPE_MESSAGES -> loadMessages()
				WIDGET_TYPE_TIMETABLE -> loadTimetable()
				else -> emptyList()
			}
		} catch (e: Exception) {
			// TODO: Implement proper error handling
		}
	}

	private fun loadMessages(): List<WidgetListItem>? {
		val query = UntisRequest.UntisRequestQuery(user ?: return null)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = applicationContext.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = Authentication.createAuthObject(user)
		))

		return runBlocking {
			val result = UntisRequest().request(query)
			result.fold({ data ->
				val untisResponse = SerializationUtils.getJSON().decodeFromString<MessageResponse>(data)
				untisResponse.result?.messages?.map {
					WidgetListItem(it.id.toLong(), it.subject, it.body)
				}?.let {
					status = STATUS_DONE
					it
				}
			}, { null }) ?: run {
				status = STATUS_ERROR
				listOf(errorItem)
			}
		}
	}

	// TODO: This function duplicates code from TimetableLoader. This should be resolved during backend refactoring.
	private fun loadTimetable(): List<WidgetListItem>? {
		val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user?.id
				?: return null)
		val today = UntisDate.fromLocalDate(LocalDate.now())
		val timeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
		val query = UntisRequest.UntisRequestQuery(user)

		//query.proxyHost = proxyHost

		val params = TimetableParams(
				user.userData.elemId,
				user.userData.elemType ?: "",
				today,
				today,
				user.masterDataTimestamp,
				0,
				emptyList(),
				if (user.anonymous) Authentication.createAuthObject() else Authentication.createAuthObject(user.user, user.key)
		)

		query.data.id = "-1"
		query.data.method = UntisApiConstants.METHOD_GET_TIMETABLE
		query.data.params = listOf(params)

		return runBlocking {
			val userDataResult = UntisRequest().request(query)
			userDataResult.fold({ data ->
				val untisResponse = SerializationUtils.getJSON().decodeFromString<TimetableResponse>(data)

				untisResponse.result?.timetable?.periods?.sortedBy { it.startDateTime.toString() }?.map {
					TimegridItem(
							it.id.toLong(),
							it.startDateTime.toDateTime(),
							it.endDateTime.toDateTime(),
							params.type,
							PeriodData(timetableDatabaseInterface, it),
							includeOrgIds = false
					).run {
						WidgetListItem(
								id,
								"${startDateTime.toString(timeFormatter)} - ${endDateTime.toString(timeFormatter)} | $title",
								arrayOf(top, bottom).filter { s -> s.isNotBlank() }.joinToString(PeriodData.ELEMENT_NAME_SEPARATOR)
						)
					}
				}?.let {
					status = STATUS_DONE
					if (it.isNotEmpty()) it else listOf(noLessonsItem)
				}
			}, { null }) ?: run {
				status = STATUS_ERROR
				listOf(errorItem)
			}
		}
	}

	override fun onCreate() {
		Log.d("Widgets", "onCreate() for widget #${appWidgetId}")
		loadItems()
	}

	override fun onDataSetChanged() {
		Log.d("Widgets", "onDataSetChanged() for widget #${appWidgetId}")
		loadItems()
	}

	override fun onDestroy() {}

	override fun getViewAt(position: Int): RemoteViews {
		return RemoteViews(applicationContext.packageName, R.layout.widget_base_item).apply {
			items?.get(position)?.let { item: WidgetListItem ->
				setTextViewText(R.id.textview_listitem_line1, item.firstLine)
				setTextViewText(
						R.id.textview_listitem_line2,
						HtmlCompat.fromHtml(item.secondLine, HtmlCompat.FROM_HTML_MODE_COMPACT)
				)
			}

			val reloadIntent = Intent()
					.putExtra(EXTRA_INT_RELOAD, status == STATUS_ERROR)
					.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
			setOnClickFillInIntent(R.id.linearlayout_widget_listitem_root, reloadIntent)
		}
	}

	override fun getCount(): Int = items?.size ?: 0

	override fun getLoadingView(): RemoteViews? = null

	override fun getViewTypeCount(): Int = 1

	override fun getItemId(position: Int): Long = items?.get(position)?.id ?: position.toLong()

	override fun hasStableIds(): Boolean = true

	data class WidgetListItem(
			val id: Long,
			val firstLine: String,
			val secondLine: String
	)
}

class WidgetRemoteViewsService : RemoteViewsService() {
	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
		return WidgetRemoteViewsFactory(this.applicationContext, intent)
	}
}*/
