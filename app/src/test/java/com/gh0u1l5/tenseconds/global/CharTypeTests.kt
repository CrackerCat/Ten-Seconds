package com.gh0u1l5.tenseconds.global

import org.junit.Test

import com.gh0u1l5.tenseconds.global.CharType.fromCharTypesToCharArray
import org.junit.Assert.assertArrayEquals

class CharTypeTests {
    private val sNumbers = "1234567890".toCharArray()
    private val sSymbols = "!?@#\$%^&*()[]{}<>~`:;,./\\\"'+_=-".toCharArray()
    private val sLowerLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    private val sUpperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    @Test
    fun testFromCharTypesToCharArray() {
        assertArrayEquals(sNumbers, CharType.NUMBERS.fromCharTypesToCharArray())
        assertArrayEquals(sSymbols, CharType.SYMBOLS.fromCharTypesToCharArray())
        assertArrayEquals(sLowerLetters, CharType.LOWER_LETTERS.fromCharTypesToCharArray())
        assertArrayEquals(sUpperLetters, CharType.UPPER_LETTERS.fromCharTypesToCharArray())
        assertArrayEquals(sLowerLetters + sUpperLetters, CharType.LETTERS.fromCharTypesToCharArray())
        assertArrayEquals(sNumbers + sSymbols + sLowerLetters + sUpperLetters, CharType.ALL.fromCharTypesToCharArray())
    }
}