package com.gh0u1l5.tenseconds.backend.bean

/**
 * This class represents an account created by the user.
 *
 * @property address The account address represented in email format, e.g. emerson@facebook.com
 * @property specification The specification used for generating a password
 */
data class Account (
        var address: String = "",
        var specification: PasswordSpec = PasswordSpec()
)