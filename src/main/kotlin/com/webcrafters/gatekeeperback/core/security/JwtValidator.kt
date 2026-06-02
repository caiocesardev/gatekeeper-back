package com.webcrafters.gatekeeperback.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service

@Service
class JwtValidator(
    private val jwtProperties: JwtProperties,
) {
    private val verifier = JWT.require(
        com.auth0.jwt.algorithms.Algorithm.HMAC256(jwtProperties.secret)
    ).build()

    fun validateToken(token: String): DecodedJWT? = try {
        verifier.verify(token)
    } catch (e: JWTVerificationException) {
        null
    }

    fun extractEmail(token: String): String? {
        val decodedJWT = validateToken(token) ?: return null
        return decodedJWT.subject
    }

    fun extractRole(token: String): String? {
        val decodedJWT = validateToken(token) ?: return null
        return decodedJWT.getClaim("role").asString()
    }
}

