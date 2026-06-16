package com.webcrafters.gatekeeperback.api.admin.controller

import com.webcrafters.gatekeeperback.api.admin.dto.AppUserResponse
import com.webcrafters.gatekeeperback.api.admin.dto.CreateManagerRequest
import com.webcrafters.gatekeeperback.api.admin.service.AdminUserService
import com.webcrafters.gatekeeperback.core.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
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
@Tag(name = "Gerenciamento de Administradores", description = "Endpoints destinados à gestão de usuários com perfil de gerente no sistema de controle de acesso.")
@SecurityRequirement(name = "bearerAuth")
class AdminUserController(
    private val adminUserService: AdminUserService,
) {
    @Operation(
        summary = "Criar novo gerente",
        description = "Cadastra um novo usuário com privilégios de gerente no sistema."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Gerente criado com sucesso"),
        ApiResponse(
            responseCode = "400",
            description = "Dados de requisição inválidos",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Erro de validação",
                    summary = "Exemplo de erro 400",
                    value = """{
                      "status": 400,
                      "title": "Bad Request",
                      "message": "Validation failed for one or more fields",
                      "errors": {
                        "email": "formato de e-mail inválido"
                      }
                    }"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Não autorizado",
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
            description = "Acesso proibido",
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
    @PostMapping
    fun createManager(@Valid @RequestBody request: CreateManagerRequest): ResponseEntity<AppUserResponse> {
        val createdManager = adminUserService.createManager(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdManager)
    }

    @Operation(
        summary = "Listar gerentes",
        description = "Retorna uma lista paginada de todos os gerentes cadastrados."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
        ApiResponse(
            responseCode = "401",
            description = "Não autorizado",
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
            description = "Acesso proibido",
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
    @GetMapping
    fun listManagers(@ParameterObject pageable: Pageable): ResponseEntity<Page<AppUserResponse>> =
        ResponseEntity.ok(adminUserService.listManagers(pageable))
}