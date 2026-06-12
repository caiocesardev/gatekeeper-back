package com.webcrafters.gatekeeperback.api.cardholder.service

import com.webcrafters.gatekeeperback.api.cardholder.dto.CardholderAccessLogResponse
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import com.webcrafters.gatekeeperback.domain.repository.RfidCredentialRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CardholderService(
    private val appUserRepository: AppUserRepository,
    private val accessLogRepository: AccessLogRepository,
    private val rfidCredentialRepository: RfidCredentialRepository,
) {
    @Transactional(readOnly = true)
    fun listOwnAccessLogs(pageable: Pageable): Page<CardholderAccessLogResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.")

        val email = authentication.name

        val appUser = appUserRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")

        if (appUser.role != Role.CARDHOLDER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas para portadores (CARDHOLDER).")
        }

        val credentials = rfidCredentialRepository.findByAppUser(appUser)
        if (credentials.isEmpty()) {
            return Page.empty(pageable)
        }

        val tagReads = credentials.map { it.hexCode }

        return accessLogRepository.findByTagReadIn(tagReads, pageable)
            .map { accessLog ->
                CardholderAccessLogResponse(
                    id = accessLog.id,
                    tagRead = accessLog.tagRead,
                    accessPointId = accessLog.accessPoint?.id,
                    accessPointDescription = accessLog.accessPoint?.locationDescription ?: "Unknown",
                    timestamp = accessLog.timestamp,
                    isGranted = accessLog.isGranted,
                    denialReason = accessLog.denialReason,
                )
            }
    }
}
