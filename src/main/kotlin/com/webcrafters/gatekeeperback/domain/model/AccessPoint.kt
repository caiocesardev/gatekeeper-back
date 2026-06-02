package com.webcrafters.gatekeeperback.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class AccessPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false, unique = true)
    val mqttIdentifier: String,

    @Column(nullable = false)
    val locationDescription: String,

    @Column(nullable = false)
    val isUnderMaintenance: Boolean = false,

    @Column
    var deletedAt: LocalDateTime? = null,
)

