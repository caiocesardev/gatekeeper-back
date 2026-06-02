package com.webcrafters.gatekeeperback

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class GatekeeperBackApplication

fun main(args: Array<String>) {
    runApplication<GatekeeperBackApplication>(*args)
}
