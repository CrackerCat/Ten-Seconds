package com.gh0u1l5.tenseconds.frontend.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.bean.PasswordSpec
import com.gh0u1l5.tenseconds.frontend.UIUtils.setDefaultButtonStyle

class AddAccountDialogFragment : DialogFragment() {

    private var identityId: String = ""

    private val onFinishedListeners = mutableListOf<(String, Account) -> Unit>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_add_account, null)
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
                .setView(view)
                .setTitle(R.string.title_dialog_add_account)
                .setPositiveButton(R.string.action_add_account, null)
                .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.cancel() }
                .create().apply {
                    setOnShowListener {
                        setDefaultButtonStyle(activity!!)
                        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
                        positive.setOnClickListener { attemptAdd(this) }
                    }
                }
    }

    fun setIdentity(identityId: String) {
        this.identityId = identityId
    }

    fun addOnFinishedListener(onFinishedListener: (String, Account) -> Unit) {
        onFinishedListeners.add(onFinishedListener)
    }

    private fun attemptAdd(dialog: AlertDialog) {
        val usernameView = dialog.findViewById<EditText>(R.id.account_username) ?: return
        val domainView = dialog.findViewById<EditText>(R.id.account_domain) ?: return

        // Reset errors.
        usernameView.error = null
        domainView.error = null

        // Store values at the time of the add attempt.
        val username = usernameView.text.toString()
        val domain = domainView.text.toString()

        // Check for a valid nickname / passphrase.
        var cancel = false
        var focus: View? = null
        when {
            username.isEmpty() -> {
                usernameView.error = getString(R.string.error_field_required)
                focus = usernameView
                cancel = true
            }
            domain.isEmpty() -> {
                domainView.error = getString(R.string.error_field_required)
                focus = domainView
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focus?.requestFocus()
        } else {
            // TODO: add UI components for PasswordSpec
            val account = Account("$username@$domain", PasswordSpec())
            Store.AccountCollection.add(identityId, account)
                    ?.addOnSuccessListener {
                        onFinishedListeners.forEach { listener ->
                            listener.invoke(it.id, account)
                        }
                        dialog.dismiss()
                    }
                    ?.addOnFailureListener { e ->
                        domainView.error = e.localizedMessage
                        domainView.requestFocus()
                    }
        }
    }
}