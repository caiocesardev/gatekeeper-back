package com.webcrafters.gatekeeperback.api.manager.service

import com.webcrafters.gatekeeperback.api.manager.dto.AccessLogResponse
import com.webcrafters.gatekeeperback.api.manager.dto.AccessPointResponse
import com.webcrafters.gatekeeperback.api.manager.dto.CreateAccessPointRequest
import com.webcrafters.gatekeeperback.api.manager.dto.CreateRfidCredentialRequest
import com.webcrafters.gatekeeperback.api.manager.dto.RfidCredentialResponse
import com.webcrafters.gatekeeperback.domain.model.AccessLog
import com.webcrafters.gatekeeperback.domain.model.AccessPoint
import com.webcrafters.gatekeeperback.domain.model.RfidCredential
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import com.webcrafters.gatekeeperback.domain.repository.RfidCredentialRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ManagerService(
    private val accessPointRepository: AccessPointRepository,
    private val rfidCredentialRepository: RfidCredentialRepository,
    private val accessLogRepository: AccessLogRepository,
    private val appUserRepository: AppUserRepository,
) {
    @Transactional
    fun createAccessPoint(request: CreateAccessPointRequest): AccessPointResponse {
        if (accessPointRepository.existsByMqttIdentifier(request.mqttIdentifier)) {
            throw IllegalArgumentException("Já existe um ponto de acesso com este identificador MQTT.")
        }

        val savedAccessPoint = accessPointRepository.save(
            AccessPoint(
                mqttIdentifier = request.mqttIdentifier,
                locationDescription = request.locationDescription,
            )
        )

        return savedAccessPoint.toResponse()
    }

    @Transactional(readOnly = true)
    fun listAccessPoints(pageable: Pageable): Page<AccessPointResponse> {
        val page = accessPointRepository.findAll(pageable)
        return PageImpl(
            page.content.filter { it.deletedAt == null }.map { it.toResponse() },
            pageable,
            page.totalElements
        )
    }

    @Transactional
    fun createRfidCredential(request: CreateRfidCredentialRequest): RfidCredentialResponse {
        if (rfidCredentialRepository.existsByHexCode(request.hexCode)) {
            throw IllegalArgumentException("Já existe uma credencial RFID com este código.")
        }

        val appUser = appUserRepository.findById(request.appUserId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")
            }

        if (appUser.role != Role.CARDHOLDER) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A credencial RFID só pode ser vinculada a um usuário com perfil de cardholder."
            )
        }

        val savedCredential = rfidCredentialRepository.save(
            RfidCredential(
                hexCode = request.hexCode,
                appUser = appUser,
            )
        )

        return savedCredential.toResponse()
    }

    @Transactional(readOnly = true)
    fun listAccessLogs(pageable: Pageable): Page<AccessLogResponse> {
        val page = accessLogRepository.findAll(pageable)
        return PageImpl(
            page.content
                .filter { it.accessPoint.deletedAt == null }
                .sortedByDescending { it.timestamp }
                .map { it.toResponse() },
            pageable,
            page.totalElements
        )
    }

    private fun AccessPoint.toResponse(): AccessPointResponse = AccessPointResponse(
        id = id,
        mqttIdentifier = mqttIdentifier,
        locationDescription = locationDescription,
        isUnderMaintenance = isUnderMaintenance,
    )

    private fun RfidCredential.toResponse(): RfidCredentialResponse = RfidCredentialResponse(
        id = id,
        hexCode = hexCode,
        appUserId = appUser.id,
        isBlocked = isBlocked,
    )

    private fun AccessLog.toResponse(): AccessLogResponse = AccessLogResponse(
        id = id,
        tagRead = tagRead,
        accessPointId = accessPoint.id,
        timestamp = timestamp,
        isGranted = isGranted,
        denialReason = denialReason,
    )
}

