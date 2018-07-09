package com.gh0u1l5.tenseconds.bean

/**
 * This class represents an account created by the user.
 *
 * @property username The username of this account, e.g. emerson
 * @property domain The domain of this account, e.g. facebook.com
 * @property specification The specification used for generating a password
 */
data class Account (
        val username: String,
        val domain: String,
        val specification: PasswordSpec
)