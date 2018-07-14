package com.gh0u1l5.tenseconds.frontend.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.getPassword
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey

class VerifyIdentityDialogFragment : BaseDialogFragment() {

    private var identityId: String = ""

    private val onFinishedListeners: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_verify_identity, null).also {
            // Add password mask programmatically to support Chinese passphrase
            val passphrase = it.findViewById<EditText>(R.id.identity_passphrase)
            passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
                .setView(view)
                .setTitle(R.string.title_dialog_verify_identity)
                .setPositiveButton(R.string.action_verify_identity, null)
                .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.cancel() }
                .create().apply {
                    setOnShowListener {
                        setButtonColors(this)
                        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
                        positive.setOnClickListener { attemptAdd(this) }
                    }
                }
    }

    fun setIdentity(identityId: String) {
        this.identityId = identityId
    }

    fun addOnFinishedListener(onFinishedListener: () -> Unit) {
        onFinishedListeners.add(onFinishedListener)
    }

    private fun attemptAdd(dialog: AlertDialog) {
        val passphraseView = dialog.findViewById<EditText>(R.id.identity_passphrase) ?: return

        // Reset errors.
        passphraseView.error = null

        // Store values at the time of the add attempt.
        val passphrase = passphraseView.getPassword()

        // Check for a valid nickname / passphrase.
        var cancel = false
        var focus: View? = null
        when {
            passphrase.isEmpty() -> {
                passphraseView.error = getString(R.string.error_field_required)
                focus = passphraseView
                cancel = true
            }
            !isPassphraseValid(passphrase) -> {
                passphraseView.error = getString(R.string.error_invalid_passphrase)
                focus = passphraseView
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focus?.requestFocus()
        } else {
            showProgress(dialog, true)
            MasterKey.verify(identityId, passphrase)
                    ?.addOnSuccessListener { match ->
                        if (match) {
                            onFinishedListeners.forEach { it.invoke() }
                            dialog.dismiss()
                        } else {
                            showProgress(dialog, false)
                            passphraseView.error = getString(R.string.error_verify_failed)
                            passphraseView.requestFocus()
                        }
                    }
                    ?.addOnFailureListener { e ->
                        showProgress(dialog, false)
                        passphraseView.error = e.localizedMessage
                        passphraseView.requestFocus()
                    }
        }
    }

    private fun isPassphraseValid(passphrase: CharArray) = passphrase.size > 4

    private fun showProgress(dialog: AlertDialog, show: Boolean) {
        // The ViewPropertyAnimator APIs are not available, so simply show
        // and hide the relevant UI components.
        val verifying = dialog.findViewById<ProgressBar>(R.id.identity_verifying) ?: return
        val form = dialog.findViewById<LinearLayout>(R.id.identity_form) ?: return
        verifying.visibility = if (show) View.VISIBLE else View.GONE
        form.visibility = if (show) View.GONE else View.VISIBLE
    }
}