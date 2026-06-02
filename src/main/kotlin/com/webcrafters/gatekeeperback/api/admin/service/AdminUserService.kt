package com.webcrafters.gatekeeperback.api.admin.service

import com.webcrafters.gatekeeperback.api.admin.dto.AppUserResponse
import com.webcrafters.gatekeeperback.api.admin.dto.CreateManagerRequest
import com.webcrafters.gatekeeperback.domain.model.AppUser
import com.webcrafters.gatekeeperback.domain.model.Role
import com.webcrafters.gatekeeperback.domain.repository.AppUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserService(
    private val appUserRepository: AppUserRepository,
) {
    @Transactional
    fun createManager(request: CreateManagerRequest): AppUserResponse {
        if (appUserRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.")
        }

        val savedUser = appUserRepository.save(
            AppUser(
                fullName = request.fullName,
                email = request.email,
                password = "",
                role = Role.MANAGER,
                isActive = false,
            )
        )

        return savedUser.toResponse()
    }

    @Transactional(readOnly = true)
    fun listManagers(pageable: Pageable): Page<AppUserResponse> {
        val allManagers = appUserRepository.findAllByRole(Role.MANAGER)
        val filtered = allManagers.filter { it.deletedAt == null }.map { it.toResponse() }

        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(filtered.size)
        val end = (start + pageable.pageSize).coerceAtMost(filtered.size)
        val content = filtered.subList(start, end)

        return PageImpl(content, pageable, filtered.size.toLong())
    }

    private fun AppUser.toResponse(): AppUserResponse = AppUserResponse(
        id = id,
        fullName = fullName,
        email = email,
        role = role,
        isActive = isActive,
    )
}

