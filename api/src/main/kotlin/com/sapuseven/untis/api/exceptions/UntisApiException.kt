package com.sapuseven.untis.api.exceptions

import com.sapuseven.untis.api.model.response.Error

class UntisApiException(error: Error?) : Throwable(error?.message) {
}
