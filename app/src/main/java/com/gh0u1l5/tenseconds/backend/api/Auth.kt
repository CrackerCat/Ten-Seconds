package com.gh0u1l5.tenseconds.backend.api

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

@Suppress("MemberVisibilityCanBePrivate")
object Auth {
    val instance: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    /**
     * true if a login attempt is in progress, false otherwise
     */
    @Volatile var isLoggingIn = false
        private set

    /**
     * Try to login into Firebase using tryLogin callback.
     */
    fun login(tryLogin: FirebaseAuth.() -> Task<AuthResult>): Task<AuthResult>? {
        if (isLoggingIn) {
            return null
        }
        isLoggingIn = true
        return instance.tryLogin().addOnCompleteListener { isLoggingIn = false }
    }
}