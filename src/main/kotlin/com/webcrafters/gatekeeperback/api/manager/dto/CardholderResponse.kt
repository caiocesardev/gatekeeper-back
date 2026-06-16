package com.webcrafters.gatekeeperback.api.manager.dto

data class CardholderResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val isActive: Boolean,
    val credentials: List<RfidCredentialResponse>? = null
)
