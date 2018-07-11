package com.gh0u1l5.tenseconds.backend.crypto

import org.junit.Test

import com.gh0u1l5.tenseconds.backend.crypto.EraseUtils.erase
import org.junit.Assert.assertArrayEquals
import javax.crypto.spec.SecretKeySpec

class EraseUtilsTests {
    @Test
    fun testEraseBytes() {
        val bytes = ByteArray(16) { it.toByte() }.apply { erase() }
        assertArrayEquals(ByteArray(16) { 0 }, bytes)
    }

    @Test
    fun testEraseChar() {
        val chars = CharArray(16) { it.toChar() }.apply { erase() }
        assertArrayEquals(CharArray(16) { '\u0000' }, chars)
    }

    @Test
    fun testEraseSecretKeySpec() {
        val key = SecretKeySpec(ByteArray(32) { 1 }, "AES").apply { erase() }
        assertArrayEquals(ByteArray(32) { 0 }, key.encoded)
    }
}