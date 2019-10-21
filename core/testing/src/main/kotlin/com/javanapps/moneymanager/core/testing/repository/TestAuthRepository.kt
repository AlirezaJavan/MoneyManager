package com.javanapps.moneymanager.core.testing.repository

import com.javanapps.moneymanager.core.data.repository.AuthRepository

/** Plain in-memory auth fake (no real hashing) for tests. */
class TestAuthRepository : AuthRepository {
    private var storedUsername: String? = null
    private var storedPassword: String? = null

    override fun hasCredentials(): Boolean = storedPassword != null

    override val username: String? get() = storedUsername

    override fun register(
        username: String,
        password: String,
    ) {
        storedUsername = username.trim()
        storedPassword = password
    }

    override fun verifyPassword(password: String): Boolean = storedPassword == password

    override fun changePassword(
        oldPassword: String,
        newPassword: String,
    ): Boolean {
        if (!verifyPassword(oldPassword)) return false
        storedPassword = newPassword
        return true
    }

    /** Helper for tests to simulate state without calling register. */
    fun setHasCredentials(has: Boolean) {
        if (has) {
            if (storedPassword == null) storedPassword = "test_password"
        } else {
            storedPassword = null
            storedUsername = null
        }
    }
}
