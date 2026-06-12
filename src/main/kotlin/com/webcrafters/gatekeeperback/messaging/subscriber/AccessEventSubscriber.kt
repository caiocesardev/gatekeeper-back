package com.webcrafters.gatekeeperback.messaging.subscriber

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.webcrafters.gatekeeperback.domain.model.AccessLog
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import com.webcrafters.gatekeeperback.domain.service.ValidationService
import com.webcrafters.gatekeeperback.messaging.publisher.AccessCommandPublisher
import jakarta.annotation.PostConstruct
import org.eclipse.paho.client.mqttv3.IMqttClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AccessEventSubscriber(
    private val mqttClient: IMqttClient, // O client MQTT injetado
    private val validationService: ValidationService,
    private val accessLogRepository: AccessLogRepository,
    private val accessPointRepository: AccessPointRepository,
    private val accessCommandPublisher: AccessCommandPublisher
) {
    private val logger = LoggerFactory.getLogger(AccessEventSubscriber::class.java)

    // Instanciação manual para evitar erros de Autowired no IntelliJ
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @PostConstruct
    fun subscribe() {
        try {
            // Volta a escutar os pedidos do hardware
            mqttClient.subscribe("gatekeeper/access/request") { topic, message ->
                handleAccessEvent(topic, String(message.payload))
            }
            logger.info("✅ Inscrito no tópico gatekeeper/access/request")
        } catch (e: Exception) {
            logger.error("❌ Erro ao se inscrever no tópico MQTT", e)
        }
    }

    internal fun handleAccessEvent(topic: String, payload: String) {
        try {
            logger.info("📥 Solicitação de acesso recebida no tópico $topic")

            // 1. Decodifica o payload JSON
            val incomingRequest = objectMapper.readValue(payload, IncomingAccessRequest::class.java)

            // 2. Executa a validação lógica das Regras de Negócio (RN01 e RN02)
            val validationResult = validationService.validateTagAccess(
                incomingRequest.tagRead,
                incomingRequest.mqttIdentifier
            )

            // 3. Busca a entidade de Ponto de Acesso
            val accessPoint = accessPointRepository.findByMqttIdentifier(incomingRequest.mqttIdentifier)
                ?: throw IllegalArgumentException("Ponto de acesso não encontrado no banco: ${incomingRequest.mqttIdentifier}")

            // 4. Cria e persiste o Log de Auditoria
            val accessLog = AccessLog(
                tagRead = incomingRequest.tagRead,
                timestamp = LocalDateTime.now(),
                isGranted = validationResult.isGranted,
                denialReason = validationResult.denialReason,
                accessPoint = accessPoint
            )
            accessLogRepository.save(accessLog)
            logger.info("💾 Log de auditoria salvo. Acesso Concedido? ${validationResult.isGranted}")

            // 5. Envia a ordem de volta para a porta (via MQTT)
            accessCommandPublisher.publishResponse(
                mqttIdentifier = incomingRequest.mqttIdentifier,
                isGranted = validationResult.isGranted,
                denialReason = validationResult.denialReason
            )

        } catch (e: Exception) {
            logger.error("❌ Erro crítico ao processar evento de acesso. Payload: $payload", e)
        }
    }

    data class IncomingAccessRequest(
        val tagRead: String,
        val mqttIdentifier: String
    )
}