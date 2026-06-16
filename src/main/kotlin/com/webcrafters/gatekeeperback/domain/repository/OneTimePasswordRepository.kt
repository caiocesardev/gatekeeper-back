package com.webcrafters.gatekeeperback.domain.repository

import com.webcrafters.gatekeeperback.domain.model.OneTimePassword
import org.springframework.data.jpa.repository.JpaRepository

interface OneTimePasswordRepository : JpaRepository<OneTimePassword, Int> {
    fun findByCodeAndIsUsedFalse(code: String): OneTimePassword?
}
