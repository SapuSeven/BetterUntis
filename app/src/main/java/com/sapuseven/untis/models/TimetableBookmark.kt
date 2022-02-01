package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class TimetableBookmark (val classId:Int, val type:String, val displayName:String, val drawableId:Int)
