package com.gh0u1l5.tenseconds.backend.crypto

import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    /**
     * A [SecretKeyFactory] which can derive a key from a passphrase using PBKDF2WithHmacSHA1.
     * @hide
     */
    private val sPBEKeyFactory by lazy {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    }

    /**
     * Derives a key based on the given specification using PBKDF2WithHmacSHA1.
     *
     * @param keySpec The specification that includes passphrase, salt, iteration number and key
     * length. Notice that this [PBEKeySpec] object will be erased immediately after the operation.
     */
    fun deriveKeyWithPBKDF2(keySpec: PBEKeySpec): ByteArray {
        try {
            return sPBEKeyFactory.generateSecret(keySpec).encoded
        } finally {
            keySpec.clearPassword()
        }
    }

    /**
     * Erases a ByteArray by filling 0
     */
    fun ByteArray.erase() {
        this.fill(0)
    }

    /**
     * Erases a CharArray by filling '\u0000'
     */
    fun CharArray.erase() {
        this.fill('\u0000')
    }

    /**
     * Erases a raw secret key using reflection.
     */
    fun SecretKeySpec.erase() {
        val keyField = this.javaClass.getDeclaredField("key").apply {
            isAccessible = true
        }
        (keyField.get(this) as ByteArray).erase()
    }

    /**
     * Calculates the SHA256 hash of a ByteArray
     */
    fun ByteArray.toSHA256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    /**
     * Converts a ByteArray to its corresponding hexadecimal string.
     */
    fun ByteArray.fromBytesToHexString(): String {
        return this.joinToString("") { String.format("%02X", it) }
    }

    /**
     * Converts a hexadecimal string to its corresponding ByteArray.
     */
    fun String.fromHexStringToBytes(): ByteArray {
        return ByteArray(this.length / 2) {
            this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
}