package com.gh0u1l5.tenseconds.backend.api

import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("MemberVisibilityCanBePrivate")
object Store {
    val instance by lazy {
        FirebaseFirestore.getInstance()
    }

    object IdentityCollection {
        private fun takeCollection(): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("identities/${user.uid}/")
        }

        fun fetchAll(success: (Map<String, Identity>) -> Unit) {
            fetchAll(success) { /* Ignore */ }
        }

        fun fetchAll(success: (Map<String, Identity>) -> Unit, failed: (Exception) -> Unit) {
            takeCollection()?.get()
                    ?.addOnSuccessListener { result ->
                        success(result.associate { identity ->
                            identity.id to identity.toObject(Identity::class.java)
                        })
                    }
                    ?.addOnFailureListener(failed)
        }

        fun add(identity: Identity, success: (DocumentReference) -> Unit) {
            add(identity, success) { /* Ignore */ }
        }

        fun add(identity: Identity, success: (DocumentReference) -> Unit, failed: (Exception) -> Unit) {
            takeCollection()?.add(identity)
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }

        fun update(identityId: String, data: Map<String, Any>, success: (Void) -> Unit) {
            update(identityId, data, success) { /* Ignore */ }
        }

        fun update(identityId: String, data: Map<String, Any>, success: (Void) -> Unit, failed: (Exception) -> Unit) {
            takeCollection()?.document(identityId)?.update(data)
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }

        fun delete(identityId: String, success: (Void) -> Unit) {
            delete(identityId, success) { /* Ignore */ }
        }

        fun delete(identityId: String, success: (Void) -> Unit, failed: (Exception) -> Unit) {
            takeCollection()?.document(identityId)?.delete()
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }
    }

    object AccountCollection {
        private fun takeCollection(identityId: String): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("accounts/${user.uid}/$identityId")
        }

        fun fetchAll(identityId: String, success: (Map<String, Account>) -> Unit) {
            fetchAll(identityId, success) { /* Ignore */ }
        }

        fun fetchAll(identityId: String, success: (Map<String, Account>) -> Unit, failed: (Exception) -> Unit) {
            takeCollection(identityId)?.get()
                    ?.addOnSuccessListener { result ->
                        success(result.associate { account ->
                            account.id to account.toObject(Account::class.java)
                        })
                    }
                    ?.addOnFailureListener(failed)
        }

        fun add(identityId: String, account: Account, success: (DocumentReference) -> Unit) {
            add(identityId, account, success) { /* Ignore */ }
        }

        fun add(identityId: String, account: Account, success: (DocumentReference) -> Unit, failed: (Exception) -> Unit) {
            takeCollection(identityId)?.add(account)
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }

        fun update(identityId: String, accountId: String, data: Map<String, Any>, success: (Void) -> Unit) {
            update(identityId, accountId, data, success) { /* Ignore */ }
        }

        fun update(identityId: String, accountId: String, data: Map<String, Any>, success: (Void) -> Unit, failed: (Exception) -> Unit) {
            takeCollection(identityId)?.document(accountId)?.update(data)
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }

        fun delete(identityId: String, accountId: String, success: (Void) -> Unit) {
            delete(identityId, accountId, success) { /* Ignore */ }
        }

        fun delete(identityId: String, accountId: String, success: (Void) -> Unit, failed: (Exception) -> Unit) {
            takeCollection(identityId)?.document(accountId)?.delete()
                    ?.addOnSuccessListener(success)
                    ?.addOnFailureListener(failed)
        }
    }
}