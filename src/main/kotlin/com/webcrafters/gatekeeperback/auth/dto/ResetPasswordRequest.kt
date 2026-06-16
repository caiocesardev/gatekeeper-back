package com.webcrafters.gatekeeperback.auth.dto

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
