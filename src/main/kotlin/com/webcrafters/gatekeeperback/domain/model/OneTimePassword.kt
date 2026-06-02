package com.webcrafters.gatekeeperback.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class OneTimePassword(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    val appUser: AppUser,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    var isUsed: Boolean = false,

    @Column
    var usedAt: LocalDateTime? = null,
)

