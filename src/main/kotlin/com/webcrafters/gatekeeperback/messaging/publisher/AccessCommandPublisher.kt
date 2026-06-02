package com.webcrafters.gatekeeperback.messaging.publisher

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AccessCommandPublisher(private val mqttClient: MqttClient) {

    private val logger = LoggerFactory.getLogger(AccessCommandPublisher::class.java)

    fun grantAccess(mqttIdentifier: String) {
        val topic = "gatekeeper/command/$mqttIdentifier"
        val payload = """{"command":"GRANT_ACCESS"}"""
        try {
            mqttClient.publish(topic, payload.toByteArray(), 1, false)
            logger.info("✅ Comando GRANT_ACCESS enviado para o tópico: $topic")
        } catch (e: MqttException) {
            logger.error("❌ Erro ao enviar comando MQTT para o tópico: $topic", e)
        }
    }
}
