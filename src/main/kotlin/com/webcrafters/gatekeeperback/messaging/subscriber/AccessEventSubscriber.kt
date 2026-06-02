package com.webcrafters.gatekeeperback.messaging.subscriber

import com.webcrafters.gatekeeperback.domain.model.AccessLog
import com.webcrafters.gatekeeperback.domain.repository.AccessLogRepository
import com.webcrafters.gatekeeperback.domain.repository.AccessPointRepository
import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory

@Component
class AccessEventSubscriber(
    private val mqttClient: IMqttClient,
    private val accessLogRepository: AccessLogRepository,
    private val accessPointRepository: AccessPointRepository,
    @Value("\${spring.mqtt.broker-url:tcp://localhost:1883}")
    private val brokerUrl: String,
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
            // Formato esperado: gatekeeper/access/{mqttIdentifier}
            // Payload: {"tagRead":"ABC123","isGranted":true,"denialReason":null}
            val parts = topic.split("/")
            if (parts.size < 3) return

            val mqttIdentifier = parts[2]
            val accessPoint = accessPointRepository.findAll()
                .find { it.mqttIdentifier == mqttIdentifier && it.deletedAt == null }
                ?: run {
                    logger.warn("Ponto de acesso não encontrado: $mqttIdentifier")
                    return
                }

            // Parse JSON simples (idealmente usar Jackson)
            val tagRead = payload.substringAfter("\"tagRead\":\"").substringBefore("\"")
            val isGranted = payload.contains("\"isGranted\":true")
            val denialReason = if (payload.contains("\"denialReason\":null")) {
                null
            } else {
                payload.substringAfter("\"denialReason\":\"").substringBefore("\"")
            }

            val accessLog = AccessLog(
                tagRead = tagRead,
                accessPoint = accessPoint,
                timestamp = LocalDateTime.now(),
                isGranted = isGranted,
                denialReason = denialReason,
            )
            accessLogRepository.save(accessLog)

            logger.info("📝 AccessLog criado: tag=$tagRead, ponto=$mqttIdentifier, concedido=$isGranted")
        } catch (e: Exception) {
            logger.error("❌ Erro ao processar evento de acesso", e)
        }
    }
}

