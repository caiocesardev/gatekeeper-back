package com.webcrafters.gatekeeperback.api.cardholder.dto

import java.time.LocalDateTime

data class CardholderAccessLogResponse(
    val id: Int?,
    val tagRead: String,
    val accessPointId: Int?,
    val accessPointDescription: String,
    val timestamp: LocalDateTime,
    val isGranted: Boolean,
    val denialReason: String?,
)

