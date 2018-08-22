package com.gh0u1l5.tenseconds.frontend.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.bean.PasswordSpec
import com.gh0u1l5.tenseconds.frontend.UIUtils.setDefaultButtonStyle
import com.gh0u1l5.tenseconds.global.CharType

class AddAccountDialogFragment : DialogFragment() {

    private var identityId: String = ""

    private val onFinishedListeners = mutableListOf<(String, Account) -> Unit>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_add_account, null)
        return AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
                .setView(view)
                .setTitle(R.string.title_dialog_add_account)
                .setPositiveButton(R.string.action_add_account, null)
                .setNegativeButton(R.string.action_cancel) { dialog, _ -> dialog.cancel() }
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
        val lengthView = dialog.findViewById<EditText>(R.id.account_password_length) ?: return

        val numbers = dialog.findViewById<CheckBox>(R.id.account_char_type_numbers) ?: return
        val symbols = dialog.findViewById<CheckBox>(R.id.account_char_type_symbols) ?: return
        val lower = dialog.findViewById<CheckBox>(R.id.account_char_type_lower_letters) ?: return
        val upper = dialog.findViewById<CheckBox>(R.id.account_char_type_upper_letters) ?: return

        // Reset errors.
        usernameView.error = null
        domainView.error = null

        // Store values at the time of the add attempt.
        val username = usernameView.text.toString()
        val domain = domainView.text.toString()
        val length = lengthView.text.toString()

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
            length.isEmpty() -> {
                lengthView.error = getString(R.string.error_field_required)
                focus = lengthView
                cancel = true
            }
            !isPasswordLengthValid(length) -> {
                lengthView.error = getString(R.string.error_invalid_password_length)
                focus = lengthView
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focus?.requestFocus()
        } else {
            val spec = PasswordSpec(length.toInt(), getCharType(numbers, symbols, lower, upper))
            val account = Account("$username@$domain", spec)
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

    private fun isPasswordLengthValid(length: String): Boolean {
        return try { length.toInt() in (1..32) } catch (_: Exception) { false }
    }

    private fun getCharType(numbers: CheckBox, symbols: CheckBox, lower: CheckBox, upper: CheckBox): Int {
        var type = 0
        if (numbers.isChecked) {
            type = type or CharType.NUMBERS
        }
        if (symbols.isChecked) {
            type = type or CharType.SYMBOLS
        }
        if (lower.isChecked) {
            type = type or CharType.LOWER_LETTERS
        }
        if (upper.isChecked) {
            type = type or CharType.UPPER_LETTERS
        }
        return type
    }
}