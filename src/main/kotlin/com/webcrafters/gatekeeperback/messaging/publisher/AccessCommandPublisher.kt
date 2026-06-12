package com.webcrafters.gatekeeperback.messaging.publisher

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AccessCommandPublisher(
    private val mqttClient: MqttClient
) {
    private val logger = LoggerFactory.getLogger(AccessCommandPublisher::class.java)

    // Instanciação manual do ObjectMapper com suporte ao Kotlin e Datas do Java 8
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun publishResponse(mqttIdentifier: String, isGranted: Boolean, denialReason: String?) {
        val topic = "gatekeeper/access/response/$mqttIdentifier"
        try {
            val responsePayload = AccessResponsePayload(
                isGranted = isGranted,
                denialReason = denialReason
            )

            val jsonPayload = objectMapper.writeValueAsString(responsePayload)

            // Usando a estrutura do MQTTv3
            val message = MqttMessage(jsonPayload.toByteArray()).apply {
                qos = 1 // Garante a entrega
            }

            mqttClient.publish(topic, message)
            logger.info("📤 Comando de validação enviado para o tópico $topic: $jsonPayload")
        } catch (e: Exception) {
            logger.error("❌ Falha ao publicar comando de resposta MQTT no tópico $topic", e)
        }
    }

    fun publishCacheUpdate(mqttIdentifier: String, allowedTags: List<String>) {
        val topic = "gatekeeper/access/sync/$mqttIdentifier"
        try {
            val cachePayload = CacheSyncPayload(allowedTags = allowedTags)
            val jsonPayload = objectMapper.writeValueAsString(cachePayload)

            // Usando a estrutura do MQTTv3
            val message = MqttMessage(jsonPayload.toByteArray()).apply {
                qos = 1
                isRetained = true // Mensagem retida no broker para quando o ESP32 voltar online
            }

            mqttClient.publish(topic, message)
            logger.info("♻️ Cache de contingência enviado para o ponto $mqttIdentifier com ${allowedTags.size} tags autorizadas.")
        } catch (e: Exception) {
            logger.error("❌ Falha ao publicar atualização de cache no tópico $topic", e)
        }
    }

    data class AccessResponsePayload(
        val isGranted: Boolean,
        val denialReason: String?
    )

    data class CacheSyncPayload(
        val allowedTags: List<String>
    )
}