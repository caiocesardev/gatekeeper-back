package com.webcrafters.gatekeeperback.api.admin.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateManagerRequest(
    @field:NotBlank(message = "O nome completo é obrigatório.")
    val fullName: String,

    @field:NotBlank(message = "O e-mail é obrigatório.")
    @field:Email(message = "O e-mail informado é inválido.")
    val email: String,
)

