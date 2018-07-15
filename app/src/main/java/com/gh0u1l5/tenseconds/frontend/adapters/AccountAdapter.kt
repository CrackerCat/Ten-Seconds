package com.gh0u1l5.tenseconds.frontend.adapters

import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey
import com.gh0u1l5.tenseconds.backend.services.LockerService
import com.gh0u1l5.tenseconds.frontend.UIUtils.setDefaultButtonStyle
import java.util.concurrent.ConcurrentHashMap

class AccountAdapter(
        val identityId: String,
        private var data: LinkedHashMap<String, Account>? = null
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    companion object {
        val sAccountAdapters = ConcurrentHashMap<String, AccountAdapter>()
    }

    class ViewHolder(val line: LinearLayout) : RecyclerView.ViewHolder(line) {
        val address: TextView = line.findViewById(R.id.line_account_address)
        val delete: ImageButton = line.findViewById(R.id.line_account_delete)
    }

    fun add(accountId: String, account: Account) {
        data?.set(accountId, account)
        notifyDataSetChanged()
    }

    fun remove(accountId: String) {
        data?.remove(accountId)
        notifyDataSetChanged()
    }

    fun refresh(notifyRefreshFinished: () -> Unit = { }) {
        Store.AccountCollection.fetchAll(identityId)
                ?.addOnSuccessListener { data ->
                    this.data = data
                    notifyDataSetChanged()
                    notifyRefreshFinished()
                }
    }

    override fun getItemCount(): Int {
        val data = data ?: return 3 // Loading
        return when {
            data.isEmpty() -> 1 // Empty Data Set
            else -> data.size   // Regular Situation
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).run {
            inflate(R.layout.line_account, parent, false) as LinearLayout
        }).apply {
            address.setOnLongClickListener {
                val accountId = line.tag as? String
                val account = data?.get(accountId)
                if (accountId != null && account != null) {
                    val context = parent.context
                    MasterKey.process(context, identityId, accountId, account) {
                        LockerService.notify(password = it)
                    }
                }
                return@setOnLongClickListener true
            }
            delete.setOnClickListener {
                AlertDialog.Builder(parent.context)
                        .setTitle(R.string.title_dialog_delete_alert)
                        .setMessage(R.string.message_dialog_delete_alert)
                        .setPositiveButton(R.string.action_delete) { _, _ ->
                            val accountId = line.tag as? String
                            if (accountId != null) {
                                Store.AccountCollection.delete(identityId, accountId)
                                        ?.addOnSuccessListener {
                                            remove(accountId)
                                        }
                            }
                        }
                        .setNegativeButton(R.string.action_cancel) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create().apply {
                            setOnShowListener {
                                setDefaultButtonStyle(parent.context)
                            }
                        }
                        .show()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when {
            data == null -> { // Loading
                val context = holder.line.context
                val background = context.getDrawable(R.drawable.background_light_blue)
                holder.address.text = ""
                holder.address.background = background
            }
            data?.size == 0 -> { // Empty Data Set
                val context = holder.line.context
                holder.address.text = context.getText(R.string.prompt_add_account)
                holder.address.background = null
            }
            else -> { // Regular Situation
                val context = holder.line.context
                val background = context.getDrawable(R.drawable.account_line_background)
                val entry = ArrayList(data!!.entries)[position]
                holder.line.tag = entry.key
                holder.address.text = entry.value.address
                holder.address.background = background
            }
        }
    }
}