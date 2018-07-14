package com.gh0u1l5.tenseconds.frontend.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        password.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@setOnEditorActionListener true
            }
            false
        }
        login_button.setOnClickListener { attemptLogin() }
    }

    override fun onStart() {
        super.onStart()
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            login_form.visibility = View.GONE
            login_invalid_message.visibility = View.VISIBLE
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (Auth.isLoggingIn) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        // Check for a valid username / password.
        var cancel = false
        var focus: View? = null
        when {
            TextUtils.isEmpty(emailStr) -> {
                email.error = getString(R.string.error_field_required)
                focus = email
                cancel = true
            }
            TextUtils.isEmpty(passwordStr) -> {
                password.error = getString(R.string.error_field_required)
                focus = password
                cancel = true
            }
            !isEmailValid(emailStr) -> {
                email.error = getString(R.string.error_invalid_email)
                focus = email
                cancel = true
            }
            !isPasswordValid(passwordStr) -> {
                password.error = getString(R.string.error_invalid_password)
                focus = password
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focus?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Auth.login {
                showProgress(true)
                signInWithEmailAndPassword(emailStr, passwordStr)
            }?.addOnCompleteListener {
                showProgress(false)
            }?.addOnSuccessListener {
                finish()
            }?.addOnFailureListener {
                when (it) {
                    is FirebaseAuthInvalidUserException,
                    is FirebaseAuthInvalidCredentialsException -> {
                        password.error = getString(R.string.error_login_failed)
                        password.requestFocus()
                    }
                    else -> {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains('@')
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // The ViewPropertyAnimator APIs are not available, so simply show
        // and hide the relevant UI components.
        login_loading.visibility = if (show) View.VISIBLE else View.GONE
        login_form.visibility = if (show) View.GONE else View.VISIBLE
    }
}
