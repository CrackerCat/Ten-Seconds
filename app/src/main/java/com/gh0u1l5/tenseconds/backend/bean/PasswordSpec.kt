package com.gh0u1l5.tenseconds.backend.bean

import com.gh0u1l5.tenseconds.global.CharType
import com.gh0u1l5.tenseconds.global.CharType.LOWER_LETTERS
import com.gh0u1l5.tenseconds.global.CharType.NUMBERS
import com.gh0u1l5.tenseconds.global.CharType.SYMBOLS
import com.gh0u1l5.tenseconds.global.CharType.UPPER_LETTERS

/**
 * This class specifies a password generation process.
 *
 * @property length The length of the generated password.
 * @property types The possible types of characters, which has to be the combination of [NUMBERS],
 * [SYMBOLS], [LOWER_LETTERS] or [UPPER_LETTERS].
 */
data class PasswordSpec (
        var length: Int = 16,
        var types: Int = CharType.ALL
)