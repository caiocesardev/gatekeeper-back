package com.webcrafters.gatekeeperback.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime


@Entity
class AppUser(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Int? = null,

	@Column(nullable = false)
	var fullName: String,

	@Column(nullable = false, unique = true)
	var email: String,

	@Column(nullable = false)
	var password: String?,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var role: Role,

	@Column(nullable = false)
	var isActive: Boolean = false,

	@Column
	var deletedAt: LocalDateTime? = null,
)


