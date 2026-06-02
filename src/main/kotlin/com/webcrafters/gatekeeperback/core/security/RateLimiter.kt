package com.webcrafters.gatekeeperback.core.security

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

@Service
class RateLimiter {
    private val attemptCache = ConcurrentHashMap<String, MutableList<Long>>()
    private val maxAttempts = 5
    private val windowSizeMs = 15 * 60 * 1000L // 15 minutos

    fun isAllowed(key: String): Boolean {
        val now = Instant.now().toEpochMilli()
        val attempts = attemptCache.getOrPut(key) { mutableListOf() }

        // Remover tentativas fora da janela
        attempts.removeAll { now - it > windowSizeMs }

        return if (attempts.size < maxAttempts) {
            attempts.add(now)
            true
        } else {
            false
        }
    }

    fun reset(key: String) {
        attemptCache.remove(key)
    }
}

