package com.gh0u1l5.tenseconds.backend.crypto

object Scrypt {
    init {
        System.loadLibrary("crypto-engine")
    }

    class ScryptException : Exception()

    /**
     * Derives a 256-bit key using scrypt algorithm, with arguments N = 32768, r = 8, p = 1
     */
    @Throws(ScryptException::class)
    external fun derive(password: CharArray, salt: ByteArray): ByteArray
}