package com.gh0u1l5.tenseconds.frontend.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.gh0u1l5.tenseconds.backend.crypto.CryptoUtils.getPassword
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey
import com.gh0u1l5.tenseconds.frontend.UIUtils.setDefaultButtonStyle

class AddIdentityDialogFragment : DialogFragment() {

    private val onFinishedListeners = mutableListOf<(String, Identity) -> Unit>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_add_identity, null).also {
            // Add password mask programmatically to support Chinese passphrase
            val passphrase = it.findViewById<EditText>(R.id.identity_passphrase)
            passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
                .setView(view)
                .setTitle(R.string.title_dialog_add_identity)
                .setPositiveButton(R.string.action_add_identity, null)
                .setNegativeButton(R.string.action_cancel) { dialog, _ -> dialog.cancel() }
                .create().apply {
                    setOnShowListener {
                        setDefaultButtonStyle(activity!!)
                        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
                        positive.setOnClickListener { attemptAdd(this) }
                    }
                }
    }

    fun addOnFinishedListener(onFinishedListener: (String, Identity) -> Unit) {
        onFinishedListeners.add(onFinishedListener)
    }

    private fun attemptAdd(dialog: AlertDialog) {
        val nicknameView = dialog.findViewById<EditText>(R.id.identity_nickname) ?: return
        val passphraseView = dialog.findViewById<EditText>(R.id.identity_passphrase) ?: return

        // Reset errors.
        nicknameView.error = null
        passphraseView.error = null

        // Store values at the time of the add attempt.
        val nickname = nicknameView.text.toString()
        val passphrase = passphraseView.getPassword()

        // Check for a valid nickname / passphrase.
        var cancel = false
        var focus: View? = null
        when {
            nickname.isEmpty() -> {
                nicknameView.error = getString(R.string.error_field_required)
                focus = nicknameView
                cancel = true
            }
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
            val identity = Identity(nickname, "")
            Store.IdentityCollection.add(identity)
                    ?.addOnSuccessListener {
                        MasterKey.update(it.id, passphrase)
                        onFinishedListeners.forEach { listener ->
                            listener.invoke(it.id, identity)
                        }
                        dialog.dismiss()
                    }
                    ?.addOnFailureListener { e ->
                        showProgress(dialog, false)
                        passphraseView.error = e.localizedMessage
                        passphraseView.requestFocus()
                    }
        }
    }

    private fun isPassphraseValid(passphrase: CharArray) = passphrase.size > 8

    private fun showProgress(dialog: AlertDialog, show: Boolean) {
        // The ViewPropertyAnimator APIs are not available, so simply show
        // and hide the relevant UI components.
        val adding = dialog.findViewById<ProgressBar>(R.id.identity_adding) ?: return
        val form = dialog.findViewById<LinearLayout>(R.id.identity_form) ?: return
        adding.visibility = if (show) View.VISIBLE else View.GONE
        form.visibility = if (show) View.GONE else View.VISIBLE
    }
}