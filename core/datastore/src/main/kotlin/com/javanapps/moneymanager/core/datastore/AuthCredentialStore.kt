package com.javanapps.moneymanager.core.datastore

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthCredentialStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        private val secretKey: SecretKey by lazy { getOrCreateKey() }

        private val prefs: SharedPreferences =
            context.getSharedPreferences("moneymanager_auth", Context.MODE_PRIVATE)

        fun hasCredentials(): Boolean = prefs.contains(KEY_PASSWORD_HASH)

        val username: String? get() = decrypt(prefs.getString(KEY_USERNAME, null))
        val passwordHash: String? get() = decrypt(prefs.getString(KEY_PASSWORD_HASH, null))
        val salt: String? get() = decrypt(prefs.getString(KEY_SALT, null))

        fun save(
            username: String,
            passwordHash: String,
            salt: String,
        ) {
            prefs.edit {
                putString(KEY_USERNAME, encrypt(username))
                putString(KEY_PASSWORD_HASH, encrypt(passwordHash))
                putString(KEY_SALT, encrypt(salt))
            }
        }

        private fun getOrCreateKey(): SecretKey {
            keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }
            val generator =
                KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore",
                )
            generator.init(
                KeyGenParameterSpec
                    .Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            return generator.generateKey()
        }

        private fun encrypt(plainText: String): String {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivBytes = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(ivBytes.size + cipherText.size)
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.size)
            System.arraycopy(cipherText, 0, combined, ivBytes.size, cipherText.size)
            return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
        }

        private fun decrypt(encryptedBase64: String?): String? {
            encryptedBase64 ?: return null
            return try {
                val combined = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)
                val ivBytes = combined.copyOfRange(0, IV_LENGTH)
                val cipherText = combined.copyOfRange(IV_LENGTH, combined.size)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH, ivBytes))
                String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
            } catch (_: Exception) {
                null
            }
        }

        private companion object {
            const val KEY_ALIAS = "moneymanager_auth_key"
            const val KEY_USERNAME = "auth_username"
            const val KEY_PASSWORD_HASH = "auth_password_hash"
            const val KEY_SALT = "auth_salt"
            const val IV_LENGTH = 12
            const val TAG_LENGTH = 128
        }
    }
