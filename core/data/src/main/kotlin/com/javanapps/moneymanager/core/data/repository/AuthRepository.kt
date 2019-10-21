package com.javanapps.moneymanager.core.data.repository

import com.javanapps.moneymanager.core.datastore.AuthCredentialStore
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Manages local credentials: username + a PBKDF2-salted password hash. */
interface AuthRepository {
    fun hasCredentials(): Boolean

    val username: String?

    fun register(
        username: String,
        password: String,
    )

    fun verifyPassword(password: String): Boolean

    fun changePassword(
        oldPassword: String,
        newPassword: String,
    ): Boolean
}

@OptIn(ExperimentalEncodingApi::class)
@Singleton
internal class DefaultAuthRepository
    @Inject
    constructor(
        private val store: AuthCredentialStore,
    ) : AuthRepository {
        override fun hasCredentials(): Boolean = store.hasCredentials()

        override val username: String? get() = store.username

        override fun register(
            username: String,
            password: String,
        ) {
            val salt = SecureRandom().generateSeed(SALT_BYTES)
            val hash = hash(password, salt)
            store.save(
                username = username.trim(),
                passwordHash = Base64.encode(hash),
                salt = Base64.encode(salt),
            )
        }

        override fun verifyPassword(password: String): Boolean {
            val saltB64 = store.salt ?: return false
            val expected = store.passwordHash ?: return false
            val computed = Base64.encode(hash(password, Base64.decode(saltB64)))
            return constantTimeEquals(computed, expected)
        }

        override fun changePassword(
            oldPassword: String,
            newPassword: String,
        ): Boolean {
            if (!verifyPassword(oldPassword)) return false
            val name = store.username ?: return false
            register(name, newPassword)
            return true
        }

        private fun hash(
            password: String,
            salt: ByteArray,
        ): ByteArray {
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        }

        private fun constantTimeEquals(
            a: String,
            b: String,
        ): Boolean {
            if (a.length != b.length) return false
            var result = 0
            for (i in a.indices) result = result or (a[i].code xor b[i].code)
            return result == 0
        }

        private companion object {
            const val ALGORITHM = "PBKDF2WithHmacSHA256"
            const val ITERATIONS = 120_000
            const val KEY_LENGTH_BITS = 256
            const val SALT_BYTES = 16
        }
    }
