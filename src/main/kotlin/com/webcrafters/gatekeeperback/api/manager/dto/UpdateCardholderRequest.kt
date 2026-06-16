package com.webcrafters.gatekeeperback.api.manager.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateCardholderRequest(
    @field:NotBlank(message = "O nome completo não pode ficar em branco.")
    val fullName: String?,

    @field:Email(message = "O e-mail informado é inválido.")
    val email: String?
)
