package com.gh0u1l5.tenseconds.backend.crypto

import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.deriveKeyWithPBKDF2
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromBytesToHexString
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.toSHA256
import com.gh0u1l5.tenseconds.backend.crypto.EraseUtils.erase
import com.gh0u1l5.tenseconds.global.Constants.PBKDF2_ITERATIONS
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * This object wraps all the cryptographic operations related to the master keys. A master key
 * is derived from a passphrase entered by the user, and will be imported into Android KeyStore
 * immediately.
 */
object MasterKey {
    private val sAndroidKeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }

    private val sMasterKeyProtection by lazy {
        KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

            setUserAuthenticationRequired(true)
            setUserAuthenticationValidityDurationSeconds(-1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setInvalidatedByBiometricEnrollment(false)
            }

            build()
        }
    }

    /**
     * Derives an AES-256 master key from a passphrase using PBKDF2.
     *
     * @param identityId The ID of the identity which owns this master key. This ID will be used as
     * salt value in PBKDF2.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    private fun derive(identityId: String, passphrase: CharArray): SecretKey {
        var keyBuffer: ByteArray? = null
        try {
            val salt = identityId.toByteArray()
            val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, 32 * 8)
            keyBuffer = deriveKeyWithPBKDF2(spec)
            return SecretKeySpec(keyBuffer, "AES")
        } finally {
            keyBuffer?.erase()
            passphrase.erase()
        }
    }

    /**
     * Updates the passphrase bounded to a specified identity. It will
     *   1. Generates an AES-256 master key based on the given passphrase.
     *   2. Stores the master key to local Android Keystore.
     *   3. Stores the hash SHA256(identityId + SHA256(key)) to FireStore.
     *
     * @param identityId The ID of the identity which owns this master key.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    fun update(identityId: String, passphrase: CharArray) {
        val key = derive(identityId, passphrase)
        try {
            // Update local storage
            val entry = KeyStore.SecretKeyEntry(key)
            sAndroidKeyStore.setEntry("$identityId-master", entry, sMasterKeyProtection)

            // Update remote storage
            val hash = (identityId.toByteArray() + key.encoded.toSHA256()).toSHA256()
            val data = mapOf("master" to hash.fromBytesToHexString())
            Store.IdentityCollection.update(identityId, data)
        } finally {
            (key as SecretKeySpec).erase()
        }
    }

    /**
     * Retrieves the AES-256 master key of the specified identity from Android KeyStore.
     *
     * @param identityId The ID of the identity which owns this master key.
     */
    private fun retrieve(identityId: String): SecretKey? {
        // TODO: handle fingerprint
        val entry = sAndroidKeyStore.getEntry("$identityId-master", null)
        // TODO: should caller erase this secret key?
        return (entry as? KeyStore.SecretKeyEntry)?.secretKey
    }
}