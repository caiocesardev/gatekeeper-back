package com.webcrafters.gatekeeperback.core.config

import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminSeeder(
    private val userRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun seedAdminUser() {
        if (!userRepository.existsByEmail("admin@gatekeeper.com")) {
            val adminUser = AppUser(
                fullName = "System Administrator",
                email = "admin@gatekeeper.com",
                password = passwordEncoder.encode("admin123"),
                role = Role.ADMIN,
                isActive = true,
            )
            userRepository.save(adminUser)
            println("Administrador padrão criado com sucesso.")
        }
    }
}