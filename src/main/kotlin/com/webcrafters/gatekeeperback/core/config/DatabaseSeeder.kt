package com.webcrafters.gatekeeperback.core.config

import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DatabaseSeeder(
    private val userRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun seedUsers() {
        // 1. Cria o usuário ADMIN se não existir
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

        // 2. Cria o usuário MANAGER de teste se não existir
        if (!userRepository.existsByEmail("manager@gatekeeper.com")) {
            val managerUser = AppUser(
                fullName = "Manager de Teste",
                email = "manager@gatekeeper.com",
                password = passwordEncoder.encode("manager123"),
                role = Role.MANAGER,
                isActive = true,
            )
            userRepository.save(managerUser)
            println("Usuário Manager de teste criado com sucesso.")
        }

        // 3. Cria o usuário CARDHOLDER de teste se não existir
        if (!userRepository.existsByEmail("user@gatekeeper.com")) {
            val cardholderUser = AppUser(
                fullName = "Portador de Teste",
                email = "user@gatekeeper.com",
                password = passwordEncoder.encode("user123"),
                role = Role.CARDHOLDER,
                isActive = true,
            )
            userRepository.save(cardholderUser)
            println("Usuário Cardholder de teste criado com sucesso.")
        }
    }
}
