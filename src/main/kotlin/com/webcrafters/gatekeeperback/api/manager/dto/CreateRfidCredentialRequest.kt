package com.webcrafters.gatekeeperback.api.manager.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateRfidCredentialRequest(
    @field:NotBlank(message = "O código hexadecimal da credencial é obrigatório.")
    val hexCode: String,

    @field:Positive(message = "O identificador do usuário deve ser maior que zero.")
    val appUserId: Int,
)

