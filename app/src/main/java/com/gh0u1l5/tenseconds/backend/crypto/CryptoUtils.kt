package com.gh0u1l5.tenseconds.backend.crypto

object CryptoUtils {
    fun ByteArray.erase() {
        this.fill(0)
    }

    fun CharArray.erase() {
        this.fill('\u0000')
    }

    fun ByteArray.toHexString(): String {
        return this.joinToString { String.format("%02X", it) }
    }
}