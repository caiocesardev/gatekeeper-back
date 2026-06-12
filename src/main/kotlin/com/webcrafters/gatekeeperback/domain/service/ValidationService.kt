package com.webcrafters.gatekeeperback.domain.service

import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.repository.RfidCredentialRepository
import org.springframework.stereotype.Service

data class ValidationResult(
    val isGranted: Boolean,
    val denialReason: String? = null
)

@Service
class ValidationService(
    private val rfidCredentialRepository: RfidCredentialRepository,
    private val accessPointRepository: AccessPointRepository
) {

    fun validateTagAccess(hexCode: String, mqttIdentifier: String): ValidationResult {
        // Verificar se o ponto de acesso físico existe no sistema
        val accessPoint = accessPointRepository.findAll()
            .find { it.mqttIdentifier == mqttIdentifier && it.deletedAt == null }
            ?: return ValidationResult(false, "PONTO_ACESSO_NAO_ENCONTRADO")

        // Verificar se o ponto de acesso está em manutenção
        if (accessPoint.isUnderMaintenance) {
            return ValidationResult(false, "PONTO_EM_MANUTENCAO")
        }

        // RN01 - Cadastro e Bloqueio: Tag deve existir e isBlocked deve ser false
        val credential = rfidCredentialRepository.findAll()
            .find { it.hexCode == hexCode && it.deletedAt == null }
            ?: return ValidationResult(false, "TAG_NAO_CADASTRADA")

        if (credential.isBlocked) {
            return ValidationResult(false, "TAG_BLOQUEADA")
        }

        // RN02 - Permissão de Local: Verificar se o usuário associado está ativo
        val user = credential.appUser
        if (!user.isActive || user.deletedAt != null) {
            return ValidationResult(false, "USUARIO_INATIVO")
        }

        // Se passar por todas as validações, o acesso é concedido
        return ValidationResult(true)
    }
}