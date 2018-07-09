package com.gh0u1l5.tenseconds.bean

import com.gh0u1l5.tenseconds.constant.CharType.NUMBERS
import com.gh0u1l5.tenseconds.constant.CharType.SYMBOLS
import com.gh0u1l5.tenseconds.constant.CharType.LOWER_LETTERS
import com.gh0u1l5.tenseconds.constant.CharType.UPPER_LETTERS

/**
 * This class specifies a password generation process.
 *
 * @property length The length of the generated password.
 * @property types The possible types of characters, which has to be the combination of [NUMBERS],
 * [SYMBOLS], [LOWER_LETTERS] or [UPPER_LETTERS].
 */
data class PasswordSpec (
        val length: Int,
        val types: Int
)