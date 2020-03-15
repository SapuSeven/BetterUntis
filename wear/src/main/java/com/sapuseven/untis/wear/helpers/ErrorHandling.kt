package com.sapuseven.untis.wear.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast

object ErrorHandling {

    //TODO: Improve error handling
    fun handleError(c: Context, error: String) {
        Toast.makeText(c, "Something went wrong!", Toast.LENGTH_LONG).show()
        Log.e("Untis", error)
    }
}