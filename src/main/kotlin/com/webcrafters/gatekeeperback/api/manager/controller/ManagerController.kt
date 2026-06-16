package com.webcrafters.gatekeeperback.api.manager.controller

import com.webcrafters.gatekeeperback.api.manager.dto.*
import com.webcrafters.gatekeeperback.api.manager.service.ManagerService
import com.webcrafters.gatekeeperback.core.exception.ErrorResponse
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Gerenciamento", description = "Endpoints destinados à administração do sistema de controle de acesso, incluindo gestão de pontos de acesso, credenciais e auditoria de logs.")
@RequestMapping("/api/manager")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MANAGER')")
class ManagerController(
    private val managerService: ManagerService,
) {
    @Operation(summary = "Criar ponto de acesso", description = "Cadastra um novo dispositivo ou local de controle de acesso (ex: Porta Principal, Catraca).")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Ponto de acesso criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 400, \"message\": \"O nome do ponto de acesso é obrigatório\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "401", description = "Não autorizado - Token ausente ou inválido", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 401, \"message\": \"Token JWT expirado ou inválido\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "403", description = "Proibido - Usuário sem permissão de administrador", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/access-points")
    fun createAccessPoint(
        @Valid @RequestBody request: CreateAccessPointRequest,
    ): ResponseEntity<AccessPointResponse> {
        val createdAccessPoint = managerService.createAccessPoint(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccessPoint)
    }

    @Operation(summary = "Listar pontos de acesso", description = "Retorna uma lista paginada de todos os pontos de acesso configurados no sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/access-points")
    fun listAccessPoints(@ParameterObject pageable: Pageable): ResponseEntity<Page<AccessPointResponse>> {
        val response = managerService.listAccessPoints(pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Criar credencial RFID", description = "Vincula uma nova tag ou cartão RFID a um usuário no sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Credencial criada com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 400, \"message\": \"Código RFID já cadastrado\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 404, \"message\": \"Usuário com ID 123 não encontrado\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/rfid-credentials")
    fun createRfidCredential(
        @Valid @RequestBody request: CreateRfidCredentialRequest,
    ): ResponseEntity<RfidCredentialResponse> {
        val createdCredential = managerService.createRfidCredential(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCredential)
    }

    @Operation(summary = "Listar logs de acesso", description = "Recupera o histórico paginado de entradas e saídas registradas pelos pontos de acesso.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Logs recuperados com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/access-logs")
    fun listAccessLogs(@ParameterObject pageable: Pageable): ResponseEntity<Page<AccessLogResponse>> {
        val response = managerService.listAccessLogs(pageable)
        return ResponseEntity.ok(response)
    }

    // --- Cardholders (Users) ---

    @Operation(
        summary = "Criar portador (Cardholder)",
        description = "Cadastra um novo usuário com o perfil de portador (Cardholder), que poderá ter credenciais RFID associadas."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Portador criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou e-mail já em uso", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 400, \"message\": \"O e-mail informado já está em uso.\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "401", description = "Não autorizado - Token ausente ou inválido", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 401, \"message\": \"Token JWT expirado ou inválido\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "403", description = "Acesso negado. Requer perfil de 'MANAGER'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/cardholders")
    fun createCardholder(
        @Valid @RequestBody request: CreateCardholderRequest,
    ): ResponseEntity<CardholderResponse> {
        val createdCardholder = managerService.createCardholder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCardholder)
    }

    @Operation(
        summary = "Listar portadores (Cardholders)",
        description = "Retorna uma lista paginada de todos os usuários com perfil de portador (Cardholder)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de portadores recuperada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 401, \"message\": \"Token JWT expirado ou inválido\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "403", description = "Acesso negado. Requer perfil de 'MANAGER'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/cardholders")
    fun listCardholders(@ParameterObject pageable: Pageable): ResponseEntity<Page<CardholderResponse>> {
        val response = managerService.listCardholders(pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Obter detalhes do portador",
        description = "Recupera os detalhes de um portador específico, incluindo a lista de suas credenciais RFID ativas."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Detalhes do portador recuperados com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 401, \"message\": \"Token JWT expirado ou inválido\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "403", description = "Acesso negado. Requer perfil de 'MANAGER'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Portador não encontrado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 404, \"message\": \"Portador com ID 99 não encontrado.\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/cardholders/{id}")
    fun getCardholder(@PathVariable id: Int): ResponseEntity<CardholderResponse> {
        val response = managerService.getCardholder(id)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Ativar ou inativar portador",
        description = "Altera o estado de um portador entre ativo e inativo. Um portador inativo tem seu acesso revogado em todos os pontos de acesso físicos (sincronização via MQTT)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Estado do portador alterado com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 401, \"message\": \"Token JWT expirado ou inválido\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "403", description = "Acesso negado. Requer perfil de 'MANAGER'.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Portador não encontrado", content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject(value = "{\"status\": 404, \"message\": \"Portador com ID 99 não encontrado.\", \"timestamp\": \"2023-10-27T10:00:00Z\"}")])]),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PatchMapping("/cardholders/{id}/status")
    fun toggleCardholderStatus(@PathVariable id: Int): ResponseEntity<CardholderResponse> {
        val response = managerService.toggleCardholderStatus(id)
        return ResponseEntity.ok(response)
    }
}
