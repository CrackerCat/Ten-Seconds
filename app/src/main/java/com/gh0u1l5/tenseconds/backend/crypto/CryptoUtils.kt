package com.gh0u1l5.tenseconds.backend.crypto

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    fun ByteArray.erase() {
        this.fill(0)
    }

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

    fun ByteArray.toSHA256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    fun ByteArray.fromBytesToHexString(): String {
        return this.joinToString("") { String.format("%02X", it) }
    }

    fun String.fromHexStringToBytes(): ByteArray {
        return ByteArray(this.length / 2) {
            this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
}