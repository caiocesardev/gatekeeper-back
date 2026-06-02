package com.webcrafters.gatekeeperback.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.webcrafters.gatekeeperback.domain.model.AppUser
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(jwtProperties.secret)

    fun generateToken(appUser: AppUser): String {
        val now = Date()
        val expiresAt = Date(now.time + jwtProperties.expirationMs)

        return JWT.create()
            .withSubject(appUser.email)
            .withClaim("role", appUser.role.name)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}

