package com.webcrafters.gatekeeperback.core.config

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MqttConfig (
    @Value("\${spring.mqtt.broker-url}") private val brokerUrl: String,
    @Value("\${spring.mqtt.client-id}") private val clientId: String,
) {

    @Bean
    fun mqttClient(): MqttClient {
        val client = MqttClient(brokerUrl, clientId, MemoryPersistence())
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            isAutomaticReconnect = true
            connectionTimeout = 10
        }
        client.connect(options)
        println("Conectado ao Broker MQTT em $brokerUrl")
        return client
    }
}