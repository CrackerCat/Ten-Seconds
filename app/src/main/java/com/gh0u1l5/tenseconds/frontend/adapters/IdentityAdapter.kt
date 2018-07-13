package com.gh0u1l5.tenseconds.frontend.adapters

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey

class IdentityAdapter(var data: List<Pair<String, Identity>>) : RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {
    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val nickname: TextView = card.findViewById(R.id.identity_card_nickname)
        val add: ImageButton = card.findViewById(R.id.identity_card_add_account)
        val delete: ImageButton = card.findViewById(R.id.identity_card_delete_identity)

        val lock: ImageView = card.findViewById(R.id.identity_card_lock)
        val loading: ProgressBar = card.findViewById(R.id.identity_card_loading)
    }

    fun refreshData(notifyRefreshFinished: () -> Unit = { }) {
        Store.IdentityCollection.fetchAll()
                ?.addOnSuccessListener { data ->
                    this.data = data
                    notifyDataSetChanged()
                    notifyRefreshFinished()
                    MasterKey.cleanup(data.map { it.first })
                }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).run {
            inflate(R.layout.card_identity, parent, false) as CardView
        }).apply {
            add.setOnClickListener {
                // TODO: popup add account dialog
            }
            delete.setOnClickListener {
                // TODO: popup alert dialog for delete
                Store.IdentityCollection.delete(card.tag as String)
                refreshData()
            }
            lock.setOnClickListener {
                // TODO: popup verify dialog
                tryUnlockIdentity(this, card.tag as String)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val identityId = data[position].first
        val identity = data[position].second

        holder.card.tag = identityId
        holder.nickname.text = identity.nickname
        tryUnlockIdentity(holder, identityId)
    }

    private fun tryUnlockIdentity(holder: ViewHolder, identityId: String) {
        if (MasterKey.retrieve(identityId) == null) {
            holder.lock.visibility = View.VISIBLE
            holder.loading.visibility = View.GONE
        } else {
            holder.lock.visibility = View.GONE
            holder.loading.visibility = View.VISIBLE
        }
    }
}