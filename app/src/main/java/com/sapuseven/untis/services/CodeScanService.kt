package com.sapuseven.untis.services

import android.net.Uri

interface CodeScanService {
	fun scanCode(onSuccess: (Uri) -> Unit);
}
