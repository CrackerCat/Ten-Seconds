package com.gh0u1l5.tenseconds.backend.bean

/**
 * This class represents an identity created by the user.
 *
 * @property nickname The human-readable nickname for displaying.
 * @property master The hash of the combination of uid and master password.
 */
data class Identity (
        val nickname: String,
        val master: String
)