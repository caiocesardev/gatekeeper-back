package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.AccessPoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AccessPointRepository : JpaRepository<AccessPoint, Int> {
	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AccessPoint a WHERE a.mqttIdentifier = ?1 AND a.deletedAt IS NULL")
	fun existsByMqttIdentifier(mqttIdentifier: String): Boolean

	@Query("SELECT a FROM AccessPoint a WHERE a.deletedAt IS NULL")
	fun findAllActive(): List<AccessPoint>

    @Query("SELECT a FROM AccessPoint a WHERE a.mqttIdentifier = ?1 AND a.deletedAt IS NULL")
    fun findByMqttIdentifier(mqttIdentifier: String): AccessPoint?
}
