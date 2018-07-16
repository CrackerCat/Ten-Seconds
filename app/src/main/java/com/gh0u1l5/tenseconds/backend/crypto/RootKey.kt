package com.gh0u1l5.tenseconds.backend.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.gh0u1l5.tenseconds.global.TenSecondsApplication.Companion.sAndroidKeyStore
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * This object wraps all the cryptographic operations related to the root keys locked in KeyStore.
 */
@Deprecated("The master keys can be imported into Android KeyStore directly.")
object RootKey {
    /**
     * The generator used to create a new AES-256 root key in Android KeyStore.
     */
    private val sRootKeyGenerator by lazy {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            init(KeyGenParameterSpec.Builder("root", purpose).run {
                setKeySize(256)
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

                setUserAuthenticationRequired(true)
                setUserAuthenticationValidityDurationSeconds(-1)
                if (Build.VERSION.SDK_INT >= 28) {
                    setIsStrongBoxBacked(true) // TODO: this has to be checked.
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
     */
    fun wrap(key: Key): Pair<ByteArray, ByteArray> {
        return Cipher.getInstance("AES/CBC/PKCS7Padding").run {
            init(Cipher.WRAP_MODE, retrieve())
            iv to wrap(key)
        }
    }

    /**
     * Unwraps a secret key using root key with AES-GCM algorithm.
     *
     * @param iv A 12 byte IV used to wrap this secret key.
     * @param wrappedKey The wrapped secret key.
     */
    fun unwrap(iv: ByteArray, wrappedKey: ByteArray): Key {
        return Cipher.getInstance("AES/CBC/PKCS7Padding").run {
            init(Cipher.UNWRAP_MODE, retrieve(), IvParameterSpec(iv))
            unwrap(wrappedKey, "AES", Cipher.SECRET_KEY)
        }
    }
}