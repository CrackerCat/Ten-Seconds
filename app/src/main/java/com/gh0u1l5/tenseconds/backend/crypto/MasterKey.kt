package com.gh0u1l5.tenseconds.backend.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.crypto.CryptoObjects.sAndroidKeyStore
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.deriveKeyWithPBKDF2
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.digestWithSHA256
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromBytesToHexString
import com.gh0u1l5.tenseconds.backend.crypto.EraseUtils.erase
import com.gh0u1l5.tenseconds.global.CharType.fromCharTypesToCharArray
import com.gh0u1l5.tenseconds.global.Constants.PBKDF2_ITERATIONS
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * This object wraps all the cryptographic operations related to the master keys. A master key
 * is derived from a passphrase entered by the user, and will be imported into Android KeyStore
 * immediately.
 */
object MasterKey {
    private val sMasterKeyProtection by lazy {
        KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT).run {
            setRandomizedEncryptionRequired(false)
            setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

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
     * @param identityId The identityId bounded to this master key, which will be used as the salt
     * value in PBKDF2.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    private fun derive(identityId: String, passphrase: CharArray): SecretKey {
        // TODO: change PBKDF2 to scrypt as soon as possible
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
     * Calculate the salted hash of a master key as SHA256(identityId + rawKey).
     *
     * @param identityId The identityId bounded to this master key
     * @param rawKey The raw master key stored in a ByteArray
     */
    private fun hash(identityId: String, rawKey: ByteArray): ByteArray {
        return digestWithSHA256(identityId.toByteArray(), rawKey)
    }

    /**
     * Updates the passphrase bounded to a specified identity. It will
     *   1. Generates an AES-256 master key based on the given passphrase.
     *   2. Stores the master key to local Android Keystore.
     *   3. Stores the hash SHA256(identityId + SHA256(key)) to FireStore.
     *
     * @param identityId The identityId bounded to this master key
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    fun update(identityId: String, passphrase: CharArray) {
        val key = derive(identityId, passphrase)
        try {
            // Update local storage
            // TODO: catch error caused by no fingerprints
            val entry = KeyStore.SecretKeyEntry(key)
            sAndroidKeyStore.setEntry("$identityId-master", entry, sMasterKeyProtection)
            // Update remote storage
            val data = mapOf("master" to hash(identityId, key.encoded).fromBytesToHexString())
            Store.IdentityCollection.update(identityId, data)
        } finally {
            (key as SecretKeySpec).erase()
        }
    }

    /**
     * Verifies that the given passphrase matches the stored hash.
     *
     * @param identityId TThe identityId bounded to this master key
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     * @param success The success callback
     */
    fun verify(identityId: String, passphrase: CharArray, success: () -> Unit) {
        val key = derive(identityId, passphrase)
        try {
            val hash = hash(identityId, key.encoded).fromBytesToHexString()
            Store.IdentityCollection.fetch(identityId)?.addOnSuccessListener {
                if (it.master == hash) {
                    success()
                }
            }
        } finally {
            (key as SecretKeySpec).erase()
        }
    }

    /**
     * Retrieves the AES-256 master key of the specified identity backed by Android KeyStore.
     *
     * @param identityId The identityId bounded to this master key
     */
    private fun retrieve(identityId: String): SecretKey? {
        val entry = sAndroidKeyStore.getEntry("$identityId-master", null)
        return (entry as? KeyStore.SecretKeyEntry)?.secretKey
    }

    /**
     * Uses the master key to generate a password for the given account.
     *
     * @param context The context for current operation
     * @param identityId The identityId that owns this account
     * @param accountId The accountId bounded to this account
     * @param account The basic account information
     * @param success The success callback, which will receive the generated password
     */
    fun access(context: Context, identityId: String, accountId: String, account: Account, success: (CharArray) -> Unit) {
        val key = retrieve(identityId) ?: throw IllegalStateException("invalid master key")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16) {
                if (it < accountId.length) accountId[it].toByte() else it.toByte()
            }))
        }
        BiometricUtils.authenticate(context, cipher, object : BiometricUtils.AuthenticationCallback {
            override fun onSuccess(cipher: Cipher) {
                val source = "$accountId#${account.username}@${account.domain}".toByteArray()
                val buffer = cipher.doFinal(source)
                val password = CharArray(account.specification.length)
                try {
                    val chars = account.specification.types.fromCharTypesToCharArray()
                    success(password.apply {
                        for (i in 0 until size) {
                            this[i] = chars[buffer[i % buffer.size] % chars.size]
                        }
                    })
                } finally {
                    buffer.erase()
                    password.erase()
                }
            }
        })
    }

    /**
     * Deletes the master key bounded by the given identityId locally.
     */
    fun delete(identityId: String) {
        sAndroidKeyStore.deleteEntry(identityId)
    }
}