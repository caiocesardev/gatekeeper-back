package com.webcrafters.gatekeeperback.core.exception

data class ErrorResponse(
    val status: Int,
    val title: String,
    val message: String,
    val errors: Map<String, String>? = null,
)

