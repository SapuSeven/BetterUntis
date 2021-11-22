package com.sapuseven.untis.models.untis.masterdata

import kotlinx.serialization.Serializable

@Serializable
data class TimetableBookmark (val classId:Int, val displayName:String, val drawableId:Int)
