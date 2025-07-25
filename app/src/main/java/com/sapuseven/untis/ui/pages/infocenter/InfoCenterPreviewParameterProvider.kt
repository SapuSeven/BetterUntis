package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.response.Error
import com.sapuseven.untis.api.model.response.UntisErrorCode
import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.model.rest.Message
import com.sapuseven.untis.ui.pages.infocenter.PreviewParameterData.messagesOfDay
import com.sapuseven.untis.ui.pages.infocenter.PreviewParameterData.untisApiException

class InfoCenterMessagesOfDayPreviewParameterProvider : PreviewParameterProvider<List<MessageOfDay>> {
	override val values: Sequence<List<MessageOfDay>> = sequenceOf(messagesOfDay)
}

class InfoCenterMessagesPreviewParameterProvider : PreviewParameterProvider<List<Message>> {
	override val values: Sequence<List<Message>> = sequenceOf()
}

private object PreviewParameterData {
	val messagesOfDay = listOf(
		MessageOfDay(
			id = 1,
			subject = "Test subject",
			body = "This is an example body with <b>HTML Formatting</b>.",
			attachments = listOf(Attachment(
				id = 1,
				name = "Test attachment",
				url = "https://sapuseven.com"
			))
		)
	)

	// TODO: Move this elsewhere when reusing
	val untisApiException = UntisApiException(
		Error(
			code = UntisErrorCode.UNKNOWN,
			message = "Loading failed: API is not supported during previews"
		)
	)
}

// TODO: Move this elsewhere when reusing
class ErrorPreviewParameterProvider : PreviewParameterProvider<Throwable> {
	override val values: Sequence<Throwable> = sequenceOf(untisApiException)
}
