package com.gh0u1l5.tenseconds.backend.api

import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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

        fun fetch(identityId: String): Task<Identity>? {
            return takeCollection()?.document(identityId)?.get()?.continueWith { task ->
                task.result.toObject(Identity::class.java)
            }
        }

        fun fetchAll(): Task<Map<String, Identity>>? {
            return takeCollection()?.get()?.continueWith { task ->
                task.result.associate { identity ->
                    identity.id to identity.toObject(Identity::class.java)
                }
            }
        }

        fun add(identity: Identity): Task<DocumentReference>? {
            return takeCollection()?.add(identity)
        }

        fun update(identityId: String, data: Map<String, Any>): Task<Void>? {
            return takeCollection()?.document(identityId)?.set(data, SetOptions.merge())
        }

        fun delete(identityId: String): Task<Void>? {
            return takeCollection()?.document(identityId)?.delete()
        }
    }

    object AccountCollection {
        private fun takeCollection(identityId: String): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("accounts/${user.uid}/$identityId/")
        }

        fun fetch(identityId: String, accountId: String): Task<Account>? {
            return takeCollection(identityId)?.document(accountId)?.get()?.continueWith { task ->
                task.result.toObject(Account::class.java)
            }
        }

        fun fetchAll(identityId: String): Task<Map<String, Account>>? {
            return takeCollection(identityId)?.get()?.continueWith { task ->
                task.result.associate { account ->
                    account.id to account.toObject(Account::class.java)
                }
            }
        }

        fun add(identityId: String, account: Account): Task<DocumentReference>? {
            return takeCollection(identityId)?.add(account)
        }

        fun update(identityId: String, accountId: String, data: Map<String, Any>): Task<Void>? {
            return takeCollection(identityId)?.document(accountId)?.set(data, SetOptions.merge())
        }

        fun delete(identityId: String, accountId: String): Task<Void>? {
            return takeCollection(identityId)?.document(accountId)?.delete()
        }
    }
}