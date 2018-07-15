package com.gh0u1l5.tenseconds.frontend.adapters

import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey
import com.gh0u1l5.tenseconds.frontend.UIUtils.setDefaultButtonStyle
import com.gh0u1l5.tenseconds.frontend.activities.MainActivity
import com.gh0u1l5.tenseconds.frontend.adapters.AccountAdapter.Companion.sAccountAdapters
import com.gh0u1l5.tenseconds.global.Constants.ACTION_ADD_ACCOUNT
import com.gh0u1l5.tenseconds.global.Constants.ACTION_VERIFY_IDENTITY

class IdentityAdapter(
        private var data: LinkedHashMap<String, Identity>
) : RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {

    companion object {
        val sIdentityAdapter = IdentityAdapter(LinkedHashMap())
    }

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val nickname: TextView = card.findViewById(R.id.identity_card_nickname)
        val add: ImageButton = card.findViewById(R.id.identity_card_add_account)
        val delete: ImageButton = card.findViewById(R.id.identity_card_delete_identity)

        val lock: ImageView = card.findViewById(R.id.identity_card_lock)
        val list: RecyclerView = card.findViewById(R.id.identity_card_account_list)
    }

    fun add(identityId: String, identity: Identity) {
        data[identityId] = identity
        notifyDataSetChanged()
    }

    fun remove(identityId: String) {
        data.remove(identityId)
        notifyDataSetChanged()
    }

    fun refresh(notifyRefreshFinished: () -> Unit = { }) {
        Store.IdentityCollection.fetchAll()
                ?.addOnSuccessListener { data ->
                    this.data = data
                    notifyDataSetChanged()
                    notifyRefreshFinished()
                    MasterKey.cleanup(data.keys)
                }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).run {
            inflate(R.layout.card_identity, parent, false) as CardView
        }).apply {
            list.layoutManager = LinearLayoutManager(parent.context)
            add.setOnClickListener {
                val context = parent.context
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent.apply {
                    action = ACTION_ADD_ACCOUNT
                    putExtra("identityId", card.tag as? String)
                })
            }
            delete.setOnClickListener {
                AlertDialog.Builder(parent.context)
                        .setTitle(R.string.title_dialog_delete_alert)
                        .setMessage(R.string.message_dialog_delete_alert)
                        .setPositiveButton(R.string.action_delete) { _, _ ->
                            val identityId = card.tag as? String
                            if (identityId != null) {
                                Store.IdentityCollection.delete(identityId)
                                        ?.addOnSuccessListener {
                                            remove(identityId)
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
            lock.setOnClickListener {
                val context = parent.context
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent.apply {
                    action = ACTION_VERIFY_IDENTITY
                    putExtra("identityId", card.tag as? String)
                })
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = ArrayList(data.entries)[position]
        holder.card.tag = entry.key
        holder.nickname.text = entry.value.nickname

        tryUnlockIdentity(holder, entry.key)
        tryDeployAccountAdapter(holder, entry.key)
    }

    private fun tryUnlockIdentity(holder: ViewHolder, identityId: String) {
        if (MasterKey.retrieve(identityId) == null) {
            holder.lock.visibility = View.VISIBLE
            holder.list.visibility = View.GONE
        } else {
            holder.lock.visibility = View.GONE
            holder.list.visibility = View.VISIBLE
        }
    }

    private fun tryDeployAccountAdapter(holder: ViewHolder, identityId: String) {
        if (sAccountAdapters[identityId] == null) {
            sAccountAdapters[identityId] = AccountAdapter(identityId)
        }
        val adapter = holder.list.adapter as? AccountAdapter
        if (adapter == null || adapter.identityId != identityId) {
            holder.list.adapter = sAccountAdapters[identityId]
        }
        sAccountAdapters[identityId]?.refresh()
    }
}