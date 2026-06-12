package com.webcrafters.gatekeeperback.messaging.subscriber

import com.webcrafters.gatekeeperback.domain.model.AccessPoint
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.service.ValidationResult
import com.webcrafters.gatekeeperback.domain.service.ValidationService
import com.webcrafters.gatekeeperback.messaging.publisher.AccessCommandPublisher
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AccessEventSubscriberTest {

    @MockK
    private lateinit var validationService: ValidationService

    @MockK
    private lateinit var accessLogRepository: AccessLogRepository

    @MockK
    private lateinit var accessPointRepository: AccessPointRepository

    @MockK
    private lateinit var accessCommandPublisher: AccessCommandPublisher

    @InjectMockKs
    private lateinit var accessEventSubscriber: AccessEventSubscriber

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

        // CORREÇÃO APLICADA AQUI TAMBÉM
        every { accessLogRepository.save(any()) } answers { firstArg() }

        justRun { accessCommandPublisher.publishResponse(mqttIdentifier, false, denialReason) }

        // Act
        accessEventSubscriber.handleAccessEvent(topic, payload)

        // Assert
        verify(exactly = 1) { accessLogRepository.save(match { !it.isGranted && it.denialReason == denialReason }) }
        verify(exactly = 1) { accessCommandPublisher.publishResponse(mqttIdentifier, false, denialReason) }
    }
}