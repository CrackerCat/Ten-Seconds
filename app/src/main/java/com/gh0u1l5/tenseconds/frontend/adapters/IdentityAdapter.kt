package com.gh0u1l5.tenseconds.frontend.adapters

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.bean.Identity

class IdentityAdapter(var data: List<Pair<String, Identity>>) : RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {
    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val nickname: TextView = card.findViewById(R.id.identity_card_nickname)
        val addAccount: ImageButton = card.findViewById(R.id.identity_card_add_account)
        val deleteIdentity: ImageButton = card.findViewById(R.id.identity_card_delete_identity)
    }

    fun refreshData(notifyRefreshFinished: () -> Unit = { }) {
        Store.IdentityCollection.fetchAll()
                ?.addOnSuccessListener {
                    data = it
                    notifyDataSetChanged()
                    notifyRefreshFinished()
                }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).run {
            inflate(R.layout.card_identity, parent, false) as CardView
        }).apply {
            addAccount.setOnClickListener {
                // TODO: popup up correct dialog
            }
            deleteIdentity.setOnClickListener {
                // TODO: add alert dialog
                Store.IdentityCollection.delete(card.tag as String)
                refreshData()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.card.tag = data[position].first
        holder.nickname.text = data[position].second.nickname
    }
}