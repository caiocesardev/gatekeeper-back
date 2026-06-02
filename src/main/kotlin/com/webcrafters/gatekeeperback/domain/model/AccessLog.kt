package com.webcrafters.gatekeeperback.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class AccessLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val tagRead: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "access_point_id", nullable = false)
    val accessPoint: AccessPoint,

    @Column(nullable = false)
    val timestamp: LocalDateTime,

    @Column(nullable = false)
    val isGranted: Boolean,

    @Column
    val denialReason: String? = null,
)

