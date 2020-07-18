package com.sapuseven.untis.widgets

import android.R
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.UntisMessage
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.MessageParams
import com.sapuseven.untis.models.untis.response.MessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

class DailyMessagesWidget : BaseWidget() {

    override fun updateAppWidget(appWidgetId: Int) {
        super.updateAppWidget(appWidgetId)
        refreshMessages(appWidgetId, user ?: return)
    }

    private fun refreshMessages(appWidgetId: Int, user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
        val newViews = loadBaseLayout(user)
        val text = SpannableStringBuilder()
        var firstLine: SpannableString
        var secondLine: String
        loadMessages(user)?.forEach {
            if (it.subject != "") {
                firstLine = SpannableString("${it.subject}\n")
                firstLine.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.primary_text_light)), 0, firstLine.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(firstLine)
            }
            secondLine = "${it.body}\n\n"
            text.append(secondLine)
        }
        newViews.setTextViewText(com.sapuseven.untis.R.id.textview_base_widget_content, text)
        appWidgetManager.updateAppWidget(appWidgetId, newViews)
    }


    private suspend fun loadMessages(user: UserDatabase.User): List<UntisMessage>? {

        val query = UntisRequest.UntisRequestQuery(user)

        query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
        query.proxyHost = context.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_connectivity_proxy_host", null)
        query.data.params = listOf(MessageParams(
                UntisDate.fromLocalDate(LocalDate.now()),
                auth = UntisAuthentication.createAuthObject(user)
        ))

        val result = UntisRequest().request(query)
        return result.fold({ data ->
            val untisResponse = SerializationUtils.getJSON().parse(MessageResponse.serializer(), data)

            untisResponse.result?.messages
        }, { null })
    }

}