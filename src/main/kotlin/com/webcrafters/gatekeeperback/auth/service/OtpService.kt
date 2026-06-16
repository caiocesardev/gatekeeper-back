package com.webcrafters.gatekeeperback.auth.service

import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.OneTimePassword
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import com.webcrafters.gatekeeperback.domain.repository.OneTimePasswordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class OtpService(
    private val otpRepository: OneTimePasswordRepository,
    private val appUserRepository: AppUserRepository,
) {
    @Transactional
    fun generateAndSendOtp(email: String): String {
        val appUser = appUserRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuário com e-mail $email não encontrado.")
        return generateAndSendOtp(appUser)
    }

    @Transactional
    fun generateAndSendOtp(appUser: AppUser): String {
        // Invalidar OTPs anteriores
        otpRepository.findAll()
            .filter { it.appUser.id == appUser.id && !it.isUsed }
            .forEach {
                it.isUsed = true
                it.usedAt = LocalDateTime.now()
                otpRepository.save(it)
            }

        // Gerar novo OTP (6 dígitos)
        val code = String.format("%06d", Random.nextInt(1000000))
        val expiresAt = LocalDateTime.now().plusMinutes(10)

        val otp = OneTimePassword(
            appUser = appUser,
            code = code,
            expiresAt = expiresAt,
        )
        otpRepository.save(otp)

        // TODO: Enviar OTP por e-mail (integração com serviço de e-mail)
        println("📧 OTP para ${appUser.email}: $code (expira em 10 minutos)")

        return code
    }

    @Transactional
    fun validateOtp(code: String): Boolean {
        val otp = otpRepository.findByCodeAndIsUsedFalse(code)
            ?: return false

        if (LocalDateTime.now() > otp.expiresAt) {
            return false
        }

        otp.isUsed = true
        otp.usedAt = LocalDateTime.now()
        otpRepository.save(otp)

        return true
    }
    
    @Transactional
    fun validateOtp(appUser: AppUser, code: String): Boolean {
        val otp = otpRepository.findByCodeAndIsUsedFalse(code)
            ?: return false
            
        if (otp.appUser.id != appUser.id) {
            return false
        }

        if (LocalDateTime.now() > otp.expiresAt) {
            return false
        }

        otp.isUsed = true
        otp.usedAt = LocalDateTime.now()
        otpRepository.save(otp)

        return true
    }
}
