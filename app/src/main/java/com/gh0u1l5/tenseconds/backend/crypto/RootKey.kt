package com.gh0u1l5.tenseconds.backend.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * This object wraps all the cryptographic operations related to the root keys locked in KeyStore.
 */
@Deprecated("The master keys can be imported into Android KeyStore directly.")
object RootKey {
    private val sAndroidKeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }

    private val sRootKeyGenerator by lazy {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            init(KeyGenParameterSpec.Builder("root", purpose).run {
                setKeySize(256)
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

                setUserAuthenticationRequired(true)
                setUserAuthenticationValidityDurationSeconds(-1)
                if (Build.VERSION.SDK_INT >= 28) {
                    setIsStrongBoxBacked(true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(false)
                }

                build()
            })
        }
    }

    /**
     * Generates an authentication-required AES-256 root key in Android KeyStore.
     */
    private fun generate(): SecretKey = sRootKeyGenerator.generateKey()

    /**
     * Retrieves the root key from Android KeyStore.
     */
    private fun retrieve(): SecretKey {
        val entry = sAndroidKeyStore.getEntry("root", null)
        return if (entry !is KeyStore.SecretKeyEntry) {
            generate()
        } else {
            entry.secretKey
        }
    }

    /**
     * Wraps a secret key using root key with AES-GCM algorithm.
     *
     * @param key The secret key to be wrapped.
     * @param iv A 12 byte buffer used to store the random-generated IV.
     */
    fun wrap(key: Key, iv: ByteArray): ByteArray {
        SecureRandom().nextBytes(iv)
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.WRAP_MODE, retrieve(), GCMParameterSpec(96, iv))
            wrap(key)
        }
    }

    /**
     * Unwraps a secret key using root key with AES-GCM algorithm.
     *
     * @param wrappedKey The wrapped secret key.
     * @param iv A 12 byte IV used to wrap this secret key.
     */
    fun unwrap(wrappedKey: ByteArray, iv: ByteArray): Key {
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.UNWRAP_MODE, retrieve(), GCMParameterSpec(96, iv))
            unwrap(wrappedKey, "", Cipher.SECRET_KEY)
        }
    }
}