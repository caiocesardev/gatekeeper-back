package com.webcrafters.gatekeeperback.api.admin.dto

import com.webcrafters.gatekeeperback.domain.model.Role

data class AppUserResponse(
    val id: Int?,
    val fullName: String,
    val email: String,
    val role: Role,
    val isActive: Boolean,
)

