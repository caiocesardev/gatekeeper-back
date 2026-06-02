package com.webcrafters.gatekeeperback.messaging.subscriber

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.webcrafters.gatekeeperback.domain.model.AccessLog
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import jakarta.annotation.PostConstruct
import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AccessEventSubscriber(
    private val mqttClient: IMqttClient,
    private val accessLogRepository: AccessLogRepository,
    private val accessPointRepository: AccessPointRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(AccessEventSubscriber::class.java)

    @PostConstruct
    fun subscribe() {
        try {
            mqttClient.subscribe("gatekeeper/access/+", IMqttMessageListener { topic, message ->
                handleAccessEvent(topic, String(message.payload))
            })
            logger.info("✅ Inscrito no tópico gatekeeper/access/+")
        } catch (e: Exception) {
            logger.error("❌ Erro ao se inscrever no tópico MQTT", e)
        }
    }

    private fun handleAccessEvent(topic: String, payload: String) {
        try {
            val mqttIdentifier = topic.substringAfter("gatekeeper/access/")
            val accessPoint = accessPointRepository.findByMqttIdentifier(mqttIdentifier)
                ?: run {
                    logger.warn("Ponto de acesso não encontrado: $mqttIdentifier")
                    return
                }

            val event = objectMapper.readValue(payload, AccessEventPayload::class.java)

            val accessLog = AccessLog(
                tagRead = event.tagRead,
                accessPoint = accessPoint,
                timestamp = LocalDateTime.now(),
                isGranted = event.isGranted,
                denialReason = event.denialReason,
            )
            accessLogRepository.save(accessLog)

            logger.info("📝 AccessLog criado: tag=${event.tagRead}, ponto=$mqttIdentifier, concedido=${event.isGranted}")
        } catch (e: Exception) {
            logger.error("❌ Erro ao processar evento de acesso do tópico $topic", e)
        }
    }

    data class AccessEventPayload(
        @JsonProperty("tagRead") val tagRead: String,
        @JsonProperty("isGranted") val isGranted: Boolean,
        @JsonProperty("denialReason") val denialReason: String?,
    )
}
