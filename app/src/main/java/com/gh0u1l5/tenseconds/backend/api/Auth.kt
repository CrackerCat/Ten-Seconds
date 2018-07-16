package com.gh0u1l5.tenseconds.backend.api

import android.app.Activity
import com.gh0u1l5.tenseconds.backend.bean.OAuthInfo
import com.gh0u1l5.tenseconds.global.Constants.RC_GOOGLE_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

    private fun retrieveGoogleOAuthToken(activity: Activity, info: OAuthInfo) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(info.clientId)
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(activity, gso)
        activity.startActivityForResult(client.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    fun retrieveOAuthToken(activity: Activity, type: OAuthType) {
        // TODO: Handle the rest situations in the future
        when (type) {
            OAuthType.Facebook -> {
            }
            OAuthType.GitHub -> {
            }
            OAuthType.Google -> {
                Store.OAuthCollection.fetch(type)
                        ?.addOnSuccessListener { info ->
                            retrieveGoogleOAuthToken(activity, info)
                        }
            }
            OAuthType.Twitter -> {
            }
        }
    }
}