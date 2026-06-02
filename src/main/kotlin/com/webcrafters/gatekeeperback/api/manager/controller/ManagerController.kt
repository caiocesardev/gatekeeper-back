package com.webcrafters.gatekeeperback.api.manager.controller

import com.webcrafters.gatekeeperback.api.manager.dto.AccessLogResponse
import com.webcrafters.gatekeeperback.api.manager.dto.AccessPointResponse
import com.webcrafters.gatekeeperback.api.manager.dto.CreateAccessPointRequest
import com.webcrafters.gatekeeperback.api.manager.dto.CreateRfidCredentialRequest
import com.webcrafters.gatekeeperback.api.manager.dto.RfidCredentialResponse
import com.webcrafters.gatekeeperback.api.manager.service.ManagerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/manager")
class ManagerController(
    private val managerService: ManagerService,
) {
    @PostMapping("/access-points")
    fun createAccessPoint(
        @Valid @RequestBody request: CreateAccessPointRequest,
    ): ResponseEntity<AccessPointResponse> {
        val createdAccessPoint = managerService.createAccessPoint(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccessPoint)
    }

    @GetMapping("/access-points")
    fun listAccessPoints(pageable: Pageable): Page<AccessPointResponse> = 
        managerService.listAccessPoints(pageable)

    @PostMapping("/rfid-credentials")
    fun createRfidCredential(
        @Valid @RequestBody request: CreateRfidCredentialRequest,
    ): ResponseEntity<RfidCredentialResponse> {
        val createdCredential = managerService.createRfidCredential(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCredential)
    }

    @GetMapping("/access-logs")
    fun listAccessLogs(pageable: Pageable): Page<AccessLogResponse> = 
        managerService.listAccessLogs(pageable)
}

