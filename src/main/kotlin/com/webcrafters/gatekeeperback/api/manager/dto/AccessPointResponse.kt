package com.webcrafters.gatekeeperback.api.manager.dto

data class AccessPointResponse(
    val id: Int?,
    val mqttIdentifier: String,
    val locationDescription: String,
    val isUnderMaintenance: Boolean,
)

