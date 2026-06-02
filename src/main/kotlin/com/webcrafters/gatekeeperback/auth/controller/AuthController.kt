package com.webcrafters.gatekeeperback.auth.controller

import com.webcrafters.gatekeeperback.auth.dto.AuthResponse
import com.webcrafters.gatekeeperback.auth.dto.LoginRequest
import com.webcrafters.gatekeeperback.auth.dto.SetupPasswordRequest
import com.webcrafters.gatekeeperback.auth.dto.ValidateOtpRequest
import com.webcrafters.gatekeeperback.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @PostMapping("/setup-password")
    fun setupPassword(@Valid @RequestBody request: SetupPasswordRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.setupPassword(request))

    @PostMapping("/validate-otp")
    fun validateOtp(@Valid @RequestBody request: ValidateOtpRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.validateOtp(request))
}

private fun AuthService.validateOtp(request: ValidateOtpRequest): AuthResponse {
    val requestClass = request.javaClass

    val method = javaClass.methods.firstOrNull { candidate ->
        candidate.name == "validateOtp" &&
            candidate.parameterTypes.size == 1 &&
            candidate.parameterTypes[0].isAssignableFrom(requestClass)
    } ?: javaClass.methods.firstOrNull { candidate ->
        candidate.name == "validateOtpAndSetPassword" &&
            candidate.parameterTypes.size == 1 &&
            candidate.parameterTypes[0].isAssignableFrom(requestClass)
    } ?: throw IllegalStateException("Não foi possível localizar um método de validação de OTP no serviço de autenticação.")

    @Suppress("UNCHECKED_CAST")
    return method.invoke(this, request) as AuthResponse
}

