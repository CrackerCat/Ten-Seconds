package com.gh0u1l5.tenseconds.frontend.adapters

import android.content.Intent
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
import com.gh0u1l5.tenseconds.frontend.activities.MainActivity
import com.gh0u1l5.tenseconds.global.Constants.ACTION_ADD_ACCOUNT
import java.util.concurrent.ConcurrentHashMap

class IdentityAdapter(private var data: LinkedHashMap<String, Identity>) :
        RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {
    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val nickname: TextView = card.findViewById(R.id.identity_card_nickname)
        val add: ImageButton = card.findViewById(R.id.identity_card_add_account)
        val delete: ImageButton = card.findViewById(R.id.identity_card_delete_identity)

        val lock: ImageView = card.findViewById(R.id.identity_card_lock)
        val list: RecyclerView = card.findViewById(R.id.identity_card_account_list)
    }

    val accountAdapters = ConcurrentHashMap<String, AccountAdapter>()
    val accountLayoutManagers = ConcurrentHashMap<String, RecyclerView.LayoutManager>()

    fun refreshData(notifyRefreshFinished: () -> Unit = { }) {
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
            add.setOnClickListener {
                val context = parent.context
                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    action = ACTION_ADD_ACCOUNT
                    putExtra("identityId", card.tag as String)
                })
            }
            delete.setOnClickListener {
                // TODO: popup alert dialog for delete
                val identityId = card.tag as String
                Store.IdentityCollection.delete(identityId)
                        ?.addOnSuccessListener {
                            data.remove(identityId)
                            notifyDataSetChanged()
                        }
            }
            lock.setOnClickListener {
                // TODO: popup verify dialog
                tryUnlockIdentity(this, card.tag as String)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = ArrayList(data.entries)[position]
        holder.card.tag = entry.key
        holder.nickname.text = entry.value.nickname

        tryUnlockIdentity(holder, entry.key)
        if (entry.key !in accountAdapters) {
            accountAdapters[entry.key] = AccountAdapter(entry.key)
        }
        holder.list.adapter = accountAdapters[entry.key]
        if (entry.key !in accountLayoutManagers) {
            accountLayoutManagers[entry.key] = LinearLayoutManager(holder.card.context)
        }
        holder.list.layoutManager = accountLayoutManagers[entry.key]
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
}