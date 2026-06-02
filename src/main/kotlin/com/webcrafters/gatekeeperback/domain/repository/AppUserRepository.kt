package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AppUserRepository : JpaRepository<AppUser, Int> {
	@Query("SELECT u FROM AppUser u WHERE u.email = ?1 AND u.deletedAt IS NULL")
	fun findByEmail(email: String): AppUser?

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM AppUser u WHERE u.email = ?1 AND u.deletedAt IS NULL")
	fun existsByEmail(email: String): Boolean

	@Query("SELECT u FROM AppUser u WHERE u.role = ?1 AND u.deletedAt IS NULL")
	fun findAllByRole(role: Role): List<AppUser>
}