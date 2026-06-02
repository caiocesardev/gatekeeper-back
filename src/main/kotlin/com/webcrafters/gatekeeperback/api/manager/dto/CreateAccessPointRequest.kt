package com.webcrafters.gatekeeperback.api.manager.dto

import jakarta.validation.constraints.NotBlank

data class CreateAccessPointRequest(
    @field:NotBlank(message = "O identificador MQTT é obrigatório.")
    val mqttIdentifier: String,

    @field:NotBlank(message = "A descrição do local é obrigatória.")
    val locationDescription: String,
)

