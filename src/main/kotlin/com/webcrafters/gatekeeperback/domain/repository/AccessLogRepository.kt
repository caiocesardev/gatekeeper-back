package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.AccessLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AccessLogRepository : JpaRepository<AccessLog, Int> {
    fun findByTagRead(tagRead: String, pageable: Pageable): Page<AccessLog>
    fun findByTagReadIn(tagReads: List<String>, pageable: Pageable): Page<AccessLog>
}
