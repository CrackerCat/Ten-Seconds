package com.gh0u1l5.tenseconds.backend.crypto

import org.junit.Test

import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.digestWithSHA256
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromBytesToHexString
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.fromHexStringToBytes
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

class CryptoUtilsTests {
    @Test fun testDigestWithSHA256() {
        val caseA = "abcdefg".toByteArray() to "7D1A54127B222502F5B79B5FB0803061152A44F92B37E23C6527BAF665D4DA9A"
        val caseB = "1234567".toByteArray() to "8BB0CF6EB9B17D0F7D22B456F121257DC1254E1F01665370476383EA776DF414"
        assertEquals(digestWithSHA256(caseA.first).fromBytesToHexString(), caseA.second)
        assertEquals(digestWithSHA256(caseB.first).fromBytesToHexString(), caseB.second)
    }

    @Test fun testFromBytesToHexString() {
        assertEquals("", byteArrayOf().fromBytesToHexString())
        assertEquals("0102030405060708090A0B0C0D0E0F", byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F).fromBytesToHexString())
        assertEquals("F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF", byteArrayOf(-0x0F, -0x0E, -0x0D, -0x0C, -0x0B, -0x0A, -0x09, -0x08, -0x07, -0x06, -0x05, -0x04, -0x03, -0x02, -0x01).fromBytesToHexString())
    }

    @Test fun testFromHexStringToBytes() {
        assertArrayEquals(byteArrayOf(), "".fromHexStringToBytes())
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F), "0102030405060708090A0B0C0D0E0F".fromHexStringToBytes())
        assertArrayEquals(byteArrayOf(-0x0F, -0x0E, -0x0D, -0x0C, -0x0B, -0x0A, -0x09, -0x08, -0x07, -0x06, -0x05, -0x04, -0x03, -0x02, -0x01), "F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF".fromHexStringToBytes())
    }
}