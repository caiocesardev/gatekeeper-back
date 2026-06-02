package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.RfidCredential
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RfidCredentialRepository : JpaRepository<RfidCredential, Int> {
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RfidCredential r WHERE r.hexCode = ?1 AND r.deletedAt IS NULL")
    fun existsByHexCode(hexCode: String): Boolean

    @Query("SELECT r FROM RfidCredential r WHERE r.appUser = ?1 AND r.deletedAt IS NULL")
    fun findByAppUser(appUser: AppUser): List<RfidCredential>
}
