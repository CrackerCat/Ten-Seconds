package com.gh0u1l5.tenseconds.backend.crypto

import android.content.Context
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.erase
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromBytesToHexString
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromHexStringToBytes
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.toSHA256
import com.gh0u1l5.tenseconds.global.Constants.PBKDF2_ITERATIONS
import com.gh0u1l5.tenseconds.global.Constants.PREF_NAME_MASTER_KEYS
import com.gh0u1l5.tenseconds.global.TenSecondsApplication
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
     * Derives an AES-256 master key from a passphrase using PBKDF2.
     *
     * @param identityId The ID of the identity which owns this master key. This ID will be used as
     * salt value in PBKDF2.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    private fun derive(identityId: String, passphrase: CharArray): SecretKey {
        var buffer: ByteArray? = null
        try {
            val salt = identityId.toByteArray()
            val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, 32 * 8)
            buffer = sPBEKeyFactory.generateSecret(spec).encoded
            return SecretKeySpec(buffer, "AES")
        } finally {
            buffer?.erase()
            passphrase.erase()
        }
    }

    /**
     * Updates the passphrase bounded to a specified identity. It will
     *   1. Generates an AES-256 master key based on the given passphrase.
     *   2. Encrypts this key using the root key and stores the encrypted value locally.
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
            val iv = ByteArray(12)
            val wrappedKey = RootKey.wrap(key, iv)
            val context = TenSecondsApplication.instance
            context.getSharedPreferences(PREF_NAME_MASTER_KEYS, Context.MODE_PRIVATE)
                    .edit()
                    .putString("${identityId}_iv", iv.fromBytesToHexString())
                    .putString("${identityId}_key", wrappedKey.fromBytesToHexString())
                    .apply()

            // Update remote storage
            val hash = (identityId.toByteArray() + key.encoded.toSHA256()).toSHA256()
            val data = mapOf("master" to hash.fromBytesToHexString())
            Store.IdentityCollection.update(identityId, data)
        } finally {
            (key as SecretKeySpec).erase()
        }
    }

    /**
     * Retrieves the AES-256 master key of the specified identity. Remember, it is caller's
     * responsibility to erase the [SecretKey] object.
     *
     * @param identityId The ID of the identity which owns this master key.
     */
    private fun retrieve(identityId: String): SecretKey? {
        // TODO: handle fingerprint

        val context = TenSecondsApplication.instance
        val masterKeys = context.getSharedPreferences(PREF_NAME_MASTER_KEYS, Context.MODE_PRIVATE)

        val ivHex = masterKeys.getString("${identityId}_iv", null)
        val wrappedKeyHex = masterKeys.getString("${identityId}_key", null)
        if (ivHex == null || wrappedKeyHex == null) {
            return null
        }

        val iv = ivHex.fromHexStringToBytes()
        val wrappedKey = wrappedKeyHex.fromHexStringToBytes()
        return RootKey.unwrap(wrappedKey, iv) as SecretKey
    }
}