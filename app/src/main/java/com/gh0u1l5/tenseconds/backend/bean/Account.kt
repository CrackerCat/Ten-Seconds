package com.gh0u1l5.tenseconds.backend.bean

/**
 * This class represents an account created by the user.
 *
 * @property username The username of this account, e.g. emerson
 * @property domain The domain of this account, e.g. facebook.com
 * @property specification The specification used for generating a password
 */
data class Account (
        var username: String,
        var domain: String,
        var specification: PasswordSpec
) {
    constructor() : this("", "", PasswordSpec())
}