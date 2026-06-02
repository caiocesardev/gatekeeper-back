package com.webcrafters.gatekeeperback.core.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

@Service
class JwtAuthenticationProvider(
    private val jwtValidator: JwtValidator,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication.credentials as String

        val email = jwtValidator.extractEmail(token)
            ?: throw BadCredentialsException("Token JWT inválido ou expirado.")

        val role = jwtValidator.extractRole(token)
            ?: throw BadCredentialsException("Token JWT sem informação de role.")

        val authorities: List<GrantedAuthority> = listOf(
            SimpleGrantedAuthority("ROLE_${role}")
        )

        return UsernamePasswordAuthenticationToken(email, token, authorities)
    }

    override fun supports(authentication: Class<*>): Boolean =
        authentication == UsernamePasswordAuthenticationToken::class.java
}

