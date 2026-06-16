package com.webcrafters.gatekeeperback.api.manager.service

import com.webcrafters.gatekeeperback.api.manager.dto.*
import com.webcrafters.gatekeeperback.domain.model.AccessLog
import com.webcrafters.gatekeeperback.domain.model.AccessPoint
import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.RfidCredential
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import com.webcrafters.gatekeeperback.domain.repository.RfidCredentialRepository
import com.webcrafters.gatekeeperback.domain.service.CacheSyncService
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
    private val cacheSyncService: CacheSyncService
) {

    // --- Access Point ---

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

        cacheSyncService.synchronizePointCache(savedAccessPoint.mqttIdentifier)

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

    // --- RFID Credential ---

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

        accessPointRepository.findAllActive().forEach { point ->
            cacheSyncService.synchronizePointCache(point.mqttIdentifier)
        }

        return savedCredential.toResponse()
    }

    // --- Access Log ---

    @Transactional(readOnly = true)
    fun listAccessLogs(pageable: Pageable): Page<AccessLogResponse> {
        val page = accessLogRepository.findAll(pageable)
        return PageImpl(
            page.content
                .filter { it.accessPoint?.deletedAt == null }
                .sortedByDescending { it.timestamp }
                .map { it.toResponse() },
            pageable,
            page.totalElements
        )
    }

    // --- Cardholder (Users) ---

    @Transactional
    fun createCardholder(request: CreateCardholderRequest): CardholderResponse {
        if (appUserRepository.findByEmail(request.email) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail.")
        }

        val appUser = AppUser(
            fullName = request.fullName,
            email = request.email,
            password = "", // Senha vazia, será definida no setup
            role = Role.CARDHOLDER,
            isActive = true
        )
        val savedUser = appUserRepository.save(appUser)

        return savedUser.toResponse()
    }

    @Transactional(readOnly = true)
    fun listCardholders(pageable: Pageable): Page<CardholderResponse> {
        val page = appUserRepository.findAll(pageable)
        val content = page.content
            .filter { it.role == Role.CARDHOLDER && it.deletedAt == null }
            .map { it.toResponse() }
        
        // Retornamos um PageImpl apenas com o conteúdo filtrado, embora a paginação no banco 
        // possa ficar incorreta devido ao filtro em memória. 
        // Em um cenário real, deveríamos usar um repositório com @Query customizada.
        return PageImpl(content, pageable, content.size.toLong())
    }

    @Transactional(readOnly = true)
    fun getCardholder(id: Int): CardholderResponse {
        val appUser = appUserRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.") }

        if (appUser.role != Role.CARDHOLDER || appUser.deletedAt != null) {
             throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado ou inativo.")
        }

        val credentials = rfidCredentialRepository.findAll()
                .filter { it.appUser.id == appUser.id && it.deletedAt == null }
                .map { it.toResponse() }

        return appUser.toResponse(credentials)
    }

    @Transactional
    fun toggleCardholderStatus(id: Int): CardholderResponse {
        val appUser = appUserRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.") }

        if (appUser.role != Role.CARDHOLDER || appUser.deletedAt != null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado ou deletado.")
        }

        appUser.isActive = !appUser.isActive
        val savedUser = appUserRepository.save(appUser)

        // REGRA DE OURO IOT: Se o usuário foi inativado, devemos sincronizar o cache
        // de TODOS os pontos ativos, para revogar as tags associadas a ele
        if (!savedUser.isActive) {
            accessPointRepository.findAllActive().forEach { point ->
                cacheSyncService.synchronizePointCache(point.mqttIdentifier)
            }
        } else {
            // Quando é ativado, também precisamos sincronizar para que ele volte a ter acesso.
            accessPointRepository.findAllActive().forEach { point ->
                cacheSyncService.synchronizePointCache(point.mqttIdentifier)
            }
        }

        return savedUser.toResponse()
    }


    // --- Extension functions ---

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
        accessPointId = accessPoint?.id,
        timestamp = timestamp,
        isGranted = isGranted,
        denialReason = denialReason,
    )

    private fun AppUser.toResponse(credentials: List<RfidCredentialResponse>? = null): CardholderResponse = CardholderResponse(
        id = id!!,
        fullName = fullName,
        email = email,
        isActive = isActive,
        credentials = credentials
    )
}
