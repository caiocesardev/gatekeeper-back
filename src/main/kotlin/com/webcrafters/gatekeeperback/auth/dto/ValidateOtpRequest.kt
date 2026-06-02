package com.webcrafters.gatekeeperback.auth.dto

import jakarta.validation.constraints.NotBlank

data class ValidateOtpRequest(
    @field:NotBlank(message = "O código OTP é obrigatório.")
    val code: String,

    @field:NotBlank(message = "A senha é obrigatória.")
    val password: String,

    @field:NotBlank(message = "O e-mail é obrigatório.")
    val email: String,
)

