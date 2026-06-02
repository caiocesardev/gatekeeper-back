package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.AccessLog
import org.springframework.data.jpa.repository.JpaRepository

interface AccessLogRepository : JpaRepository<AccessLog, Int>

