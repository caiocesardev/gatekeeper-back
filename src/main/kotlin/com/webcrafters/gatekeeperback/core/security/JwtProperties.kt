package com.webcrafters.gatekeeperback.core.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val expirationMs: Long,
)


