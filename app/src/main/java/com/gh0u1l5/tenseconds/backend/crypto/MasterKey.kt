package com.gh0u1l5.tenseconds.backend.crypto

import android.content.Context

import com.gh0u1l5.tenseconds.global.Constants.PBKDF2_ITERATIONS
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.erase
import com.gh0u1l5.tenseconds.global.TenSecondsApplication
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.toHexString
import javax.crypto.SecretKey

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * This object wraps all the cryptographic operations related to the master keys. A master key
 * is derived from a passphrase entered by the user, and will be encrypted using the root keys
 * locked in KeyStore before storing in the device storage.
 */
object MasterKey {
    /**
     * A [SecretKeyFactory] which can derive a key from a passphrase using PBKDF2.
     * @hide
     */
    private val sPBEKeyFactory by lazy {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    }

    /**
     * Derives a AES-256 master key from a passphrase using PBKDF2.
     *
     * @param identityId The ID of the identity which owns this master key. This ID will be used as
     * salt value in PBKDF2.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * derivation, the passphrase will be erased immediately.
     */
    private fun derive(identityId: String, passphrase: CharArray): ByteArray {
        val salt = identityId.toByteArray()
        try {
            return sPBEKeyFactory.generateSecret(
                    PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, 32 * 8)
            ).encoded
        } finally {
            passphrase.erase()
        }
    }

    fun generate(identityId: String, passphrase: CharArray): SecretKey {
        val key = SecretKeySpec(derive(identityId, passphrase), "AES")

        val iv = ByteArray(12)
        val wrappedKey = RootKey.wrap(key, iv)

        TenSecondsApplication.instance.getSharedPreferences("master_keys", Context.MODE_PRIVATE)
                .edit()
                .putString("${identityId}_iv", iv.toHexString())
                .putString("${identityId}_key", wrappedKey.toHexString())
                .apply()

        return key
    }

    fun retrieve(identityId: String, passphrase: CharArray) {

    }
}