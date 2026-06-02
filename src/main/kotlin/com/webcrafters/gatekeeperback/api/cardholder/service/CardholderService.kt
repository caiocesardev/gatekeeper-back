package com.webcrafters.gatekeeperback.api.cardholder.service

import com.webcrafters.gatekeeperback.api.cardholder.dto.CardholderAccessLogResponse
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CardholderService(
    private val appUserRepository: AppUserRepository,
    private val accessLogRepository: AccessLogRepository,
) {
    @Transactional(readOnly = true)
    fun listOwnAccessLogs(): List<CardholderAccessLogResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.")

        val email = authentication.name

        val appUser = appUserRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")

        if (appUser.role != Role.CARDHOLDER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas para cardholders.")
        }

        return accessLogRepository.findAll()
            .filter { accessLog -> accessLog.tagRead.equals(appUser.email, ignoreCase = true) }
            .sortedByDescending { it.timestamp }
            .map { accessLog ->
                CardholderAccessLogResponse(
                    id = accessLog.id,
                    tagRead = accessLog.tagRead,
                    accessPointId = accessLog.accessPoint.id,
                    accessPointDescription = accessLog.accessPoint.locationDescription,
                    timestamp = accessLog.timestamp,
                    isGranted = accessLog.isGranted,
                    denialReason = accessLog.denialReason,
                )
            }
    }
}

