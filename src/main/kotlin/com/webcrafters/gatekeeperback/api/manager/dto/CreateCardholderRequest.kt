package com.webcrafters.gatekeeperback.api.manager.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateCardholderRequest(
    @field:NotBlank(message = "O nome completo é obrigatório.")
    val fullName: String,

    @field:Email(message = "O e-mail informado é inválido.")
    @field:NotBlank(message = "O e-mail é obrigatório.")
    val email: String
)
