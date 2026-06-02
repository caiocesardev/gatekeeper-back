package com.webcrafters.gatekeeperback.api.manager.dto

data class RfidCredentialResponse(
    val id: Int?,
    val hexCode: String,
    val appUserId: Int?,
    val isBlocked: Boolean,
)

