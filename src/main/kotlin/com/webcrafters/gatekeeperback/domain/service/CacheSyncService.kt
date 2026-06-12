package com.webcrafters.gatekeeperback.domain.service

import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.repository.RfidCredentialRepository
import com.webcrafters.gatekeeperback.messaging.publisher.AccessCommandPublisher
import org.springframework.stereotype.Service

@Service
class CacheSyncService(
    private val rfidCredentialRepository: RfidCredentialRepository,
    private val accessPointRepository: AccessPointRepository,
    private val accessCommandPublisher: AccessCommandPublisher
) {

    fun synchronizePointCache(mqttIdentifier: String) {
        // Busca o ponto de acesso ativo
        val accessPoint = accessPointRepository.findAll()
            .find { it.mqttIdentifier == mqttIdentifier && it.deletedAt == null } ?: return

        if (accessPoint.isUnderMaintenance) {
            // Se está em manutenção, limpa o cache local removendo o acesso de todos em modo offline
            accessCommandPublisher.publishCacheUpdate(mqttIdentifier, emptyList())
            return
        }

        // Filtra todas as credenciais ativas, não bloqueadas e que possuem permissão de acesso
        // (No estágio atual, considerando acesso global para usuários ativos)
        val authorizedTags = rfidCredentialRepository.findAll()
            .filter { it.deletedAt == null && !it.isBlocked && it.appUser.isActive }
            .map { it.hexCode }

        // Dispara o payload para o hardware persistir na Flash
        accessCommandPublisher.publishCacheUpdate(mqttIdentifier, authorizedTags)
    }
}