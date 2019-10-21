package com.javanapps.moneymanager.core.domain.auth

import com.javanapps.moneymanager.core.data.repository.AuthRepository
import javax.inject.Inject

/** Whether the user has already created credentials (decides sign-up vs login). */
class HasCredentialsUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(): Boolean = repository.hasCredentials()
    }

/** Creates the initial username + password. */
class RegisterCredentialsUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            username: String,
            password: String,
        ) = repository.register(username.trim(), password)
    }

/** Verifies a password against the stored salted hash. */
class VerifyPasswordUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(password: String): Boolean = repository.verifyPassword(password)
    }

/** Changes the password after verifying the old one. */
class ChangePasswordUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            oldPassword: String,
            newPassword: String,
        ): Boolean = repository.changePassword(oldPassword, newPassword)
    }
