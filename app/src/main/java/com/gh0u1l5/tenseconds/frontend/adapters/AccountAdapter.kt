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
        private val identityId: String,
        private var data: LinkedHashMap<String, Account>? = null
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    class ViewHolder(val line: LinearLayout) : RecyclerView.ViewHolder(line) {
        val username: TextView = line.findViewById(R.id.line_account_username)
        val domain: TextView = line.findViewById(R.id.line_account_domain)
        val body: LinearLayout = line.findViewById(R.id.line_account_body)
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

    override fun getItemCount() = data?.size ?: 3

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).run {
            inflate(R.layout.line_account, parent, false) as LinearLayout
        }).apply {
            body.setOnLongClickListener {
                val accountId = line.tag as String
                val account = data?.get(accountId)
                if (account != null) {
                    MasterKey.process(parent.context, identityId, accountId, account) {
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
        if (data == null) {
            val context = holder.line.context
            holder.username.text = ""
            holder.username.background = context.getDrawable(R.drawable.input_background_light_blue)
            holder.domain.text = ""
            holder.domain.background = context.getDrawable(R.drawable.input_background_light_blue)
        } else {
            val entry = ArrayList(data!!.entries)[position]
            holder.line.tag = entry.key
            holder.username.minWidth = 0
            holder.username.text = entry.value.username
            holder.username.background = null
            holder.domain.text = entry.value.domain
            holder.domain.background = null
        }
    }
}