package com.webcrafters.gatekeeperback.api.manager.dto

import java.time.LocalDateTime

data class AccessLogResponse(
    val id: Int?,
    val tagRead: String,
    val accessPointId: Int?,
    val timestamp: LocalDateTime,
    val isGranted: Boolean,
    val denialReason: String?,
)

