package com.webcrafters.gatekeeperback.api.cardholder.controller

import com.webcrafters.gatekeeperback.api.cardholder.dto.CardholderAccessLogResponse
import com.webcrafters.gatekeeperback.api.cardholder.service.CardholderService
import com.webcrafters.gatekeeperback.core.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cardholder")
@Tag(name = "Portador de Cartão", description = "Endpoints destinados às operações e consultas realizadas pelo próprio portador do cartão de acesso.")
@SecurityRequirement(name = "bearerAuth")
class CardholderController(
    private val cardholderService: CardholderService,
) {

    @Operation(
        summary = "Listar logs de acesso próprios",
        description = "Recupera o histórico paginado de todas as tentativas de acesso (sucesso ou falha) registradas para o usuário autenticado."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de logs recuperada com sucesso"),
        ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado ou token inválido",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Token Ausente ou Inválido",
                    summary = "Exemplo de erro 401",
                    value = """{
                      "status": 401,
                      "title": "Unauthorized",
                      "message": "Token JWT ausente ou expirado",
                      "errors": null
                    }"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Usuário não possui permissão para acessar estes registros",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Permissão Negada",
                    summary = "Exemplo de erro 403",
                    value = """{
                      "status": 403,
                      "title": "Forbidden",
                      "message": "Acesso negado. Perfil insuficiente para esta operação.",
                      "errors": null
                    }"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno no servidor",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Erro Interno",
                    summary = "Exemplo de erro 500",
                    value = """{
                      "status": 500,
                      "title": "Internal Server Error",
                      "message": "Ocorreu um erro inesperado no servidor.",
                      "errors": null
                    }"""
                )]
            )]
        )
    ])
    @GetMapping("/access-logs")
    fun listOwnAccessLogs(@ParameterObject pageable: Pageable): ResponseEntity<Page<CardholderAccessLogResponse>> =
        ResponseEntity.ok(cardholderService.listOwnAccessLogs(pageable))
}