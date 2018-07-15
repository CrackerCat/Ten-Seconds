package com.gh0u1l5.tenseconds.backend.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.crypto.CryptoObjects.sAndroidKeyStore
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.digestWithSHA256
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromBytesToHexString
import com.gh0u1l5.tenseconds.backend.crypto.EraseUtils.erase
import com.gh0u1l5.tenseconds.global.CharType.fromCharTypesToCharArray
import com.google.android.gms.tasks.Task
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread

/**
 * This object wraps all the cryptographic operations related to the master keys. A master key
 * is derived from a passphrase entered by the user, and will be imported into Android KeyStore
 * immediately.
 */
object MasterKey {
    /**
     * The protection rules specified for the imported AES-256 master keys.
     */
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
     * Calculate the salted hash of a master key as SHA256(identityId + rawKey).
     *
     * @param identityId The identityId bounded to this master key
     * @param rawKey The raw master key stored in a ByteArray.
     */
    private fun hash(identityId: String, rawKey: ByteArray): ByteArray {
        return digestWithSHA256(identityId.toByteArray(), rawKey)
    }

    /**
     * Derives an AES-256 master key from a passphrase using scrypt.
     *
     * @param identityId The identityId bounded to this master key, which will be used as the salt
     * value in scrypt.
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    private fun derive(identityId: String, passphrase: CharArray): ByteArray {
        return CryptoUtils.scrypt(passphrase, identityId.toByteArray())
    }

    /**
     * Stores a AES-256 master key to both local and remote locations.
     *
     * @param identityId The identityId bounded to this master key
     * @param rawKey The AES-256 master key to be stored. Notice that after this operation, this key
     * will be erased immediately.
     */
    private fun store(identityId: String, rawKey: ByteArray) {
        val key = SecretKeySpec(rawKey, "AES")
        try {
            // Update local storage
            val entry = KeyStore.SecretKeyEntry(key)
            sAndroidKeyStore.setEntry("$identityId-master", entry, sMasterKeyProtection)
            // Update remote storage
            val data = mapOf("master" to hash(identityId, rawKey).fromBytesToHexString())
            Store.IdentityCollection.update(identityId, data)
        } catch (e: KeyStoreException) {
            Log.w(javaClass.simpleName, e)
        } finally {
            rawKey.erase()
            key.erase()
        }
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
        store(identityId, derive(identityId, passphrase))
    }

    /**
     * Verifies that the given passphrase matches the stored hash.
     *
     * @param identityId TThe identityId bounded to this master key
     * @param passphrase The passphrase used to derive the AES-256 key. Notice that after this
     * operation, this passphrase will be erased immediately.
     */
    fun verify(identityId: String, passphrase: CharArray): Task<Boolean>? {
        val key = derive(identityId, passphrase)
        val hash = hash(identityId, key).fromBytesToHexString()
        return Store.IdentityCollection.fetch(identityId)
                ?.continueWith { it.result.master == hash }
                ?.addOnSuccessListener { match ->
                    if (match) store(identityId, key)
                }
    }

    /**
     * Retrieves the AES-256 master key of the specified identity backed by Android KeyStore.
     *
     * @param identityId The identityId bounded to this master key
     */
    fun retrieve(identityId: String): SecretKey? {
        val entry = sAndroidKeyStore.getEntry("$identityId-master", null)
        return (entry as? KeyStore.SecretKeyEntry)?.secretKey
    }

    /**
     * Uses the master key to generate a password for the given account. Notice that it is caller's
     * responsibility to clean the generated password.
     *
     * @param context The context for current operation
     * @param identityId The identityId that owns this account
     * @param accountId The accountId bounded to this account
     * @param account The basic account information
     * @param success The success callback, which will receive the generated password
     */
    fun process(context: Context, identityId: String, accountId: String, account: Account, success: (CharArray) -> Unit) {
        val key = retrieve(identityId) ?: throw IllegalStateException("invalid master key")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16) {
                if (it < accountId.length) accountId[it].toByte() else it.toByte()
            }))
        }
        BiometricUtils.authenticate(context, cipher, object : BiometricUtils.AuthenticationCallback {
            override fun onSuccess(cipher: Cipher) {
                val source = account.address.toByteArray()
                val buffer = cipher.doFinal(source)
                val digest = digestWithSHA256(buffer)
                try {
                    val password = CharArray(account.specification.length)
                    val chars = account.specification.types.fromCharTypesToCharArray()
                    success(password.apply {
                        for (i in 0 until size) {
                            val value = digest[i % digest.size].toInt() and 0xFF
                            this[i] = chars[value % chars.size]
                        }
                    })
                } finally {
                    buffer.erase()
                    digest.erase()
                }
            }
        })
    }

    /**
     * Delete the master key bounded to the specific identity.
     */
    fun delete(identityId: String) {
        sAndroidKeyStore.deleteEntry("$identityId-master")
    }

    /**
     * Deletes all the master keys that are no longer used.
     *
     * @param identityIds The list of alive identities.
     */
    fun cleanup(identityIds: Set<String>) {
        thread(start = true) {
            sAndroidKeyStore.aliases().iterator().forEach { alias ->
                val identityId = alias.removeSuffix("-master")
                if (identityId !in identityIds) {
                    sAndroidKeyStore.deleteEntry(alias)
                }
            }
        }
    }
}