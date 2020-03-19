package com.sapuseven.untis.wear.helpers

import android.content.Context
import android.widget.Toast
import com.sapuseven.untis.R

object ErrorHandling {

    fun handleError(c: Context, errorCode: Int) {
        Toast.makeText(c, errorCode, Toast.LENGTH_LONG).show()
    }

    const val ACQUIRING_APP_SHARED_SECRET_FAILED = R.string.connect_acquiring_app_shared_secret_failed
    const val ACQUIRING_USER_DATA_FAILED = R.string.connect_acquiring_user_data_failed
    const val ADDING_USER_FAILED = R.string.connect_adding_user_failed
}