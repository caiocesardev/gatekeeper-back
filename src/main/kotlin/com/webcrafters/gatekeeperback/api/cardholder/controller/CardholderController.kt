package com.webcrafters.gatekeeperback.api.cardholder.controller

import com.webcrafters.gatekeeperback.api.cardholder.dto.CardholderAccessLogResponse
import com.webcrafters.gatekeeperback.api.cardholder.service.CardholderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cardholder")
class CardholderController(
    private val cardholderService: CardholderService,
) {
    @GetMapping("/access-logs")
    fun listOwnAccessLogs(): List<CardholderAccessLogResponse> =
        cardholderService.listOwnAccessLogs()
}

