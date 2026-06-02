package com.webcrafters.gatekeeperback.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SetupPasswordRequest(
    @field:Email(message = "O e-mail informado é inválido.")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória.")
    val password: String,
)

