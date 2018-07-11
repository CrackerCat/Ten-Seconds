package com.gh0u1l5.tenseconds.global

object CharType {
    const val NUMBERS: Int       = 0x00000001
    const val SYMBOLS: Int       = 0x00000002
    const val LOWER_LETTERS: Int = 0x00000004
    const val UPPER_LETTERS: Int = 0x00000008

    const val LETTERS = LOWER_LETTERS or UPPER_LETTERS
    const val ALL = NUMBERS or SYMBOLS or LETTERS

    private val sNumbers = "1234567890".toCharArray()
    private val sSymbols = "!?@#\$%^&*()[]{}<>~`:;,./\\\"'+_=-".toCharArray()
    private val sLowerLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    private val sUpperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    fun Int.fromCharTypesToCharArray(): CharArray =
            if (this and NUMBERS != 0) sNumbers else charArrayOf() +
            if (this and SYMBOLS != 0) sSymbols else charArrayOf() +
            if (this and LOWER_LETTERS != 0) sLowerLetters else charArrayOf() +
            if (this and UPPER_LETTERS != 0) sUpperLetters else charArrayOf()
}