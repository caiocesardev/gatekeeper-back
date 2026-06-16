package com.webcrafters.gatekeeperback.messaging.subscriber

import com.webcrafters.gatekeeperback.domain.model.AccessPoint
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.service.ValidationResult
import com.webcrafters.gatekeeperback.domain.service.ValidationService
import com.webcrafters.gatekeeperback.messaging.publisher.AccessCommandPublisher
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.eclipse.paho.client.mqttv3.IMqttClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AccessEventSubscriberTest {

    @MockK
    private lateinit var mqttClient: IMqttClient

    @MockK
    private lateinit var validationService: ValidationService

    @MockK
    private lateinit var accessLogRepository: AccessLogRepository

    @MockK
    private lateinit var accessPointRepository: AccessPointRepository

    @MockK
    private lateinit var accessCommandPublisher: AccessCommandPublisher

    // A anotação @InjectMockKs foi removida para evitarmos o bug de reflexão
    private lateinit var accessEventSubscriber: AccessEventSubscriber

    @BeforeEach
    fun setUp() {
        // Instanciamos manualmente a classe, injetando os mocks pelo construtor de forma segura
        accessEventSubscriber = AccessEventSubscriber(
            mqttClient = mqttClient,
            validationService = validationService,
            accessLogRepository = accessLogRepository,
            accessPointRepository = accessPointRepository,
            accessCommandPublisher = accessCommandPublisher
        )
    }

    @Test
    fun `CT01 - deve conceder acesso e publicar comando de abertura quando validacao passar`() {
        // Arrange
        val mqttIdentifier = "GATE_01"
        val tagRead = "HEXCODE123"
        val topic = "gatekeeper/access/request"
        val payload = """
            {
              "tagRead": "$tagRead",
              "mqttIdentifier": "$mqttIdentifier"
            }
        """.trimIndent()

        val mockAccessPoint = AccessPoint(id = 1, mqttIdentifier = mqttIdentifier, locationDescription = "Entrada Principal")

        every { validationService.validateTagAccess(tagRead, mqttIdentifier) } returns ValidationResult(true)
        every { accessPointRepository.findByMqttIdentifier(mqttIdentifier) } returns mockAccessPoint
        every { accessLogRepository.save(any()) } answers { firstArg() }
        justRun { accessCommandPublisher.publishResponse(mqttIdentifier, true, null) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, payload)

        // Assert
        verify(exactly = 1) { accessLogRepository.save(match { it.isGranted }) }
        verify(exactly = 1) { accessCommandPublisher.publishResponse(mqttIdentifier, true, null) }
    }

    @Test
    fun `CT02 e CT03 - deve negar acesso e publicar comando de bloqueio quando validacao falhar`() {
        // Arrange
        val mqttIdentifier = "GATE_01"
        val tagRead = "HEXCODE_INVALID"
        val topic = "gatekeeper/access/request"
        val payload = """
            {
              "tagRead": "$tagRead",
              "mqttIdentifier": "$mqttIdentifier"
            }
        """.trimIndent()

        val mockAccessPoint = AccessPoint(id = 1, mqttIdentifier = mqttIdentifier, locationDescription = "Entrada Principal")
        val denialReason = "TAG_NAO_CADASTRADA"

        every { validationService.validateTagAccess(tagRead, mqttIdentifier) } returns ValidationResult(false, denialReason)
        every { accessPointRepository.findByMqttIdentifier(mqttIdentifier) } returns mockAccessPoint
        every { accessLogRepository.save(any()) } answers { firstArg() }
        justRun { accessCommandPublisher.publishResponse(mqttIdentifier, false, denialReason) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, payload)

        // Assert
        verify(exactly = 1) { accessLogRepository.save(match { !it.isGranted && it.denialReason == denialReason }) }
        verify(exactly = 1) { accessCommandPublisher.publishResponse(mqttIdentifier, false, denialReason) }
    }

    @Test
    fun `CT04 - deve lidar com AccessPoint nao encontrado`() {
        // Arrange
        val mqttIdentifier = "GATE_01"
        val tagRead = "HEXCODE123"
        val topic = "gatekeeper/access/request"
        val payload = """
            {
              "tagRead": "$tagRead",
              "mqttIdentifier": "$mqttIdentifier"
            }
        """.trimIndent()

        every { validationService.validateTagAccess(tagRead, mqttIdentifier) } returns ValidationResult(true)
        every { accessPointRepository.findByMqttIdentifier(mqttIdentifier) } returns null
        justRun { accessCommandPublisher.publishResponse(mqttIdentifier, false, "ACCESS_POINT_NOT_FOUND") }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, payload)

        // Assert
        verify(exactly = 0) { accessLogRepository.save(any()) } // Não deve salvar log se AccessPoint não for encontrado
        verify(exactly = 1) { accessCommandPublisher.publishResponse(mqttIdentifier, false, "ACCESS_POINT_NOT_FOUND") }
    }

    @Test
    fun `CT05 - deve lidar com payload mal formatado`() {
        // Arrange
        val topic = "gatekeeper/access/request"
        val malformedPayload = """
            {
              "tagRead": "HEXCODE123",
              "mqttIdentifier": "GATE_01",
              "extraField": "value"
            }
        """.trimIndent() // Payload com campo extra, que pode causar erro de parsing se não for robusto

        // Não precisamos mockar nenhum serviço de validação ou repositório,
        // pois o erro deve ocorrer antes de interagir com eles.
        // Apenas garantimos que o publisher não seja chamado com sucesso.
        justRun { accessCommandPublisher.publishResponse(any(), any(), any()) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, malformedPayload)

        // Assert
        // Verifica que nenhum log de acesso foi salvo
        verify(exactly = 0) { accessLogRepository.save(any()) }
        // Verifica que o publisher foi chamado com uma resposta de falha e a mensagem de erro esperada
        verify(exactly = 1) {
            accessCommandPublisher.publishResponse(
                "GATE_01", false, "INVALID_PAYLOAD_FORMAT"
            )
        }
    }

    @Test
    fun `CT06 - deve lidar com excecao inesperada durante o processamento`() {
        // Arrange
        val mqttIdentifier = "GATE_01"
        val tagRead = "HEXCODE123"
        val topic = "gatekeeper/access/request"
        val payload = """
            {
              "tagRead": "$tagRead",
              "mqttIdentifier": "$mqttIdentifier"
            }
        """.trimIndent()

        // Simula uma exceção inesperada ao tentar validar o acesso
        every { validationService.validateTagAccess(tagRead, mqttIdentifier) } throws RuntimeException("Erro inesperado!")
        justRun { accessCommandPublisher.publishResponse(any(), any(), any()) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, payload)

        // Assert
        // Verifica que nenhum log de acesso foi salvo
        verify(exactly = 0) { accessLogRepository.save(any()) }
        // Verifica que o publisher foi chamado com uma resposta de falha e a mensagem de erro esperada
        verify(exactly = 1) {
            accessCommandPublisher.publishResponse(
                mqttIdentifier, false, "INTERNAL_SERVER_ERROR"
            )
        }
    }

    @Test
    fun `CT07 - deve lidar com payload mal formatado e mqttIdentifier ausente`() {
        // Arrange
        val topic = "gatekeeper/access/request"
        val malformedPayload = """
            {
              "tagRead": "HEXCODE123",
              "extraField": "value"
            }
        """.trimIndent() // Payload sem mqttIdentifier

        // Não precisamos mockar nenhum serviço de validação ou repositório,
        // pois o erro deve ocorrer antes de interagir com eles.
        // Apenas garantimos que o publisher não seja chamado com sucesso.
        justRun { accessCommandPublisher.publishResponse(any(), any(), any()) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, malformedPayload)

        // Assert
        // Verifica que nenhum log de acesso foi salvo
        verify(exactly = 0) { accessLogRepository.save(any()) }
        // Verifica que o publisher NÃO foi chamado, pois não há mqttIdentifier para responder
        verify(exactly = 0) {
            accessCommandPublisher.publishResponse(any(), any(), any())
        }
    }
}