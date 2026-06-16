package com.webcrafters.gatekeeperback.core.config

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuração central para a comunicação com o Broker MQTT.
 *
 * Esta classe é responsável por inicializar e disponibilizar o [MqttClient]
 * que será utilizado por toda a aplicação para assinar tópicos (receber eventos dos dispositivos)
 * e publicar mensagens (enviar comandos aos dispositivos).
 */
@Configuration
class MqttConfig(
    @Value("\${spring.mqtt.broker-url}") private val brokerUrl: String,
    @Value("\${spring.mqtt.client-id}") private val clientId: String,
) {

    /**
     * Cria e conecta o cliente MQTT ao broker configurado.
     *
     * O cliente é configurado com:
     * - `isCleanSession = true`: O broker não lembrará do estado da sessão anterior ao desconectar.
     * - `isAutomaticReconnect = true`: O cliente tentará se reconectar automaticamente caso a conexão caia.
     * - `connectionTimeout = 10`: Tempo máximo de espera para estabelecer a conexão (em segundos).
     *
     * @return A instância de [MqttClient] conectada.
     */
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