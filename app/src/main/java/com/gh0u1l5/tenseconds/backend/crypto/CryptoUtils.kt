package com.gh0u1l5.tenseconds.backend.crypto

import android.widget.EditText
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

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
     * A [MessageDigest] object which can generate SHA-256 digests safely.
     * @hide
     */
    private val sSHA256MessageDigest by lazy {
        MessageDigest.getInstance("SHA-256")
    }

    /**
     * Calculates the SHA256 hash of a ByteArray
     */
    fun digestWithSHA256(vararg arrays: ByteArray): ByteArray {
        try {
            arrays.forEach { array ->
                sSHA256MessageDigest.update(array)
            }
            return sSHA256MessageDigest.digest()
        } finally {
            sSHA256MessageDigest.reset()
        }
    }

    fun EditText.getPassword(): CharArray {
        return CharArray(text.length).also {
            text.getChars(0, text.length, it, 0)
        }
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