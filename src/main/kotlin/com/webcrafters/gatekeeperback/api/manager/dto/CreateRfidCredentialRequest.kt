package com.webcrafters.gatekeeperback.api.manager.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "Objeto de requisição para vincular uma nova credencial RFID a um usuário existente.")
data class CreateRfidCredentialRequest(
    @field:Schema(description = "Código hexadecimal único da tag ou cartão RFID.", example = "A1B2C3D4")
    @field:NotBlank(message = "O código hexadecimal da credencial é obrigatório.")
    val hexCode: String,

    @field:Schema(description = "ID do usuário (AppUser) ao qual esta credencial será associada.", example = "101")
    @field:NotNull(message = "O ID do usuário é obrigatório.")
    val appUserId: Int,
)