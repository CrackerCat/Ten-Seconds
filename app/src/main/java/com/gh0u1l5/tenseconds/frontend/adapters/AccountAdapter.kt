package com.gh0u1l5.tenseconds.frontend.adapters

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

class AccountAdapter(
        val identityId: String,
        private var data: LinkedHashMap<String, Account>? = null
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    class ViewHolder(val line: LinearLayout) : RecyclerView.ViewHolder(line) {
        val address: TextView = line.findViewById(R.id.line_account_address)
        val delete: ImageButton = line.findViewById(R.id.line_account_delete)
    }

    fun refreshData(notifyRefreshFinished: () -> Unit = { }) {
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
                val context = parent.context
                val accountId = line.tag as String
                val account = data?.get(accountId)
                if (account != null) {
                    MasterKey.process(context, identityId, accountId, account) {
                        LockerService.activate(password = it)
                    }
                }
                return@setOnLongClickListener true
            }
            delete.setOnClickListener {
                // TODO: popup alert dialog for delete
                val accountId = line.tag as? String ?: return@setOnClickListener
                Store.AccountCollection.delete(identityId, accountId)
                        ?.addOnSuccessListener {
                            data?.remove(accountId)
                            notifyDataSetChanged()
                        }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when {
            data == null -> { // Loading
                val context = holder.line.context
                val background = context.getDrawable(R.drawable.input_background_light_blue)
                holder.address.text = ""
                holder.address.background = background
            }
            data?.size == 0 -> { // Empty Data Set
                val context = holder.line.context
                holder.address.text = context.getText(R.string.prompt_add_account)
                holder.address.background = null
            }
            else -> { // Regular Situation
                val entry = ArrayList(data!!.entries)[position]
                holder.line.tag = entry.key
                holder.address.text = entry.value.address
                holder.address.background = null
            }
        }
    }
}