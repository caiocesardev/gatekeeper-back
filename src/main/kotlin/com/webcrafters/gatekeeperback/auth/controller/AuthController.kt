package com.webcrafters.gatekeeperback.auth.controller

import com.webcrafters.gatekeeperback.auth.dto.AuthResponse
import com.webcrafters.gatekeeperback.auth.dto.LoginRequest
import com.webcrafters.gatekeeperback.auth.dto.SetupPasswordRequest
import com.webcrafters.gatekeeperback.auth.dto.ValidateOtpRequest
import com.webcrafters.gatekeeperback.core.exception.ErrorResponse
import com.webcrafters.gatekeeperback.auth.service.AuthService
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints responsáveis pela gestão de acesso, validação de identidade e segurança dos usuários do sistema Gatekeeper.")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário no sistema utilizando e-mail e senha, retornando o token de acesso e informações de perfil."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        ApiResponse(
            responseCode = "400",
            description = "Dados da requisição inválidos",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Erro de Validação",
                    value = """{"status": 400, "message": "O e-mail informado possui formato inválido.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas ou não autorizadas",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Falha na Autenticação",
                    value = """{"status": 401, "message": "E-mail ou senha incorretos.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno no servidor",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class)
            )]
        )
    ])
    @Tag(name = "Público")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @Operation(
        summary = "Configurar senha inicial",
        description = "Permite que um novo usuário defina sua senha de acesso após o primeiro contato ou convite no sistema de controle de acesso."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Senha configurada com sucesso"),
        ApiResponse(
            responseCode = "400",
            description = "Token inválido ou senha fora dos padrões",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Senha Fraca",
                    value = """{"status": 400, "message": "A senha deve conter ao menos 8 caracteres, letras e números.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    value = """{"status": 404, "message": "Usuário não localizado para o token fornecido.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        )
    ])
    @Tag(name = "Público")
    @PostMapping("/setup-password")
    fun setupPassword(@Valid @RequestBody request: SetupPasswordRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.setupPassword(request))

    @Operation(
        summary = "Validar OTP (One-Time Password)",
        description = "Valida o código temporário enviado ao usuário para verificação de identidade em duas etapas ou recuperação de conta."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Código OTP validado com sucesso"),
        ApiResponse(
            responseCode = "400",
            description = "Dados da requisição malformados",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    value = """{"status": 400, "message": "O código OTP deve ter 6 dígitos.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Código OTP inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    value = """{"status": 401, "message": "Código expirado. Solicite um novo código.", "timestamp": "2023-10-27T10:00:00Z"}"""
                )]
            )]
        )
    ])
    @Tag(name = "Público")
    @PostMapping("/validate-otp")
    fun validateOtp(@Valid @RequestBody request: ValidateOtpRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.validateOtp(request))
}

private fun AuthService.validateOtp(request: ValidateOtpRequest): AuthResponse {
    val requestClass = request.javaClass

    val method = javaClass.methods.firstOrNull { candidate ->
        candidate.name == "validateOtp" &&
            candidate.parameterTypes.size == 1 &&
            candidate.parameterTypes[0].isAssignableFrom(requestClass)
    } ?: javaClass.methods.firstOrNull { candidate ->
        candidate.name == "validateOtpAndSetPassword" &&
            candidate.parameterTypes.size == 1 &&
            candidate.parameterTypes[0].isAssignableFrom(requestClass)
    } ?: throw IllegalStateException("Não foi possível localizar um método de validação de OTP no serviço de autenticação.")

    @Suppress("UNCHECKED_CAST")
    return method.invoke(this, request) as AuthResponse
}
