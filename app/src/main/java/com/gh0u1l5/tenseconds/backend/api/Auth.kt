package com.gh0u1l5.tenseconds.backend.api

import com.gh0u1l5.tenseconds.backend.api.TaskDecorators.withFailureLog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

@Suppress("MemberVisibilityCanBePrivate")
object Auth {
    val instance: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    enum class OAuthType {
        Facebook,
        GitHub,
        Google,
        Twitter,
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

        return instance.tryLogin()
                .addOnCompleteListener { isLoggingIn = false }
                .withFailureLog("FireAuth")
    }

    fun retrieveOAuthToken(type: OAuthType) {
        when (type) {
            OAuthType.Facebook -> {

            }
            OAuthType.GitHub -> {

            }
            OAuthType.Google -> {

            }
            OAuthType.Twitter -> {

            }
        }
    }
}