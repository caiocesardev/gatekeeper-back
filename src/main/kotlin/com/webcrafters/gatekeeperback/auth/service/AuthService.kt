package com.webcrafters.gatekeeperback.auth.service

import com.webcrafters.gatekeeperback.auth.dto.*
import com.webcrafters.gatekeeperback.core.security.JwtService
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val otpService: OtpService,
) {
    @Transactional
    fun setupPassword(request: SetupPasswordRequest): AuthResponse {
        val appUser = appUserRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")

        appUser.password = passwordEncoder.encode(request.password).toString()
        appUser.isActive = true
        appUserRepository.save(appUser)

        return AuthResponse(
            message = "Senha configurada com sucesso.",
            token = jwtService.generateToken(appUser),
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val appUser = appUserRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.")

        if (!appUser.isActive) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário ainda não está ativo.")
        }

        if (!passwordEncoder.matches(request.password, appUser.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.")
        }

        return AuthResponse(
            message = "Autenticação realizada com sucesso.",
            token = jwtService.generateToken(appUser),
        )
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest) {
        val appUser = appUserRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")
        otpService.generateAndSendOtp(appUser)
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest): AuthResponse {
        val appUser = appUserRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")

        if (!otpService.validateOtp(appUser, request.code)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código OTP inválido ou expirado.")
        }

        appUser.password = passwordEncoder.encode(request.newPassword)
        appUserRepository.save(appUser)

        return AuthResponse(
            message = "Senha redefinida com sucesso.",
            token = jwtService.generateToken(appUser)
        )
    }
}