package com.webcrafters.gatekeeperback.api.admin.controller

import com.webcrafters.gatekeeperback.api.admin.dto.AppUserResponse
import com.webcrafters.gatekeeperback.api.admin.dto.CreateManagerRequest
import com.webcrafters.gatekeeperback.api.admin.service.AdminUserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/managers")
class AdminUserController(
    private val adminUserService: AdminUserService,
) {
    @PostMapping
    fun createManager(@Valid @RequestBody request: CreateManagerRequest): ResponseEntity<AppUserResponse> {
        val createdManager = adminUserService.createManager(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdManager)
    }

    @GetMapping
    fun listManagers(pageable: Pageable): Page<AppUserResponse> = adminUserService.listManagers(pageable)
}

