package com.sapuseven.untis.api.exception

import com.sapuseven.untis.api.model.response.Error

class UntisApiException(val error: Error?) : Throwable(error?.message) {
}
