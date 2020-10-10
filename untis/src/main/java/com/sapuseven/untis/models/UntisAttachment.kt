package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisAttachment(
        var id: Int,
        var name: String,
        var url: String
)
