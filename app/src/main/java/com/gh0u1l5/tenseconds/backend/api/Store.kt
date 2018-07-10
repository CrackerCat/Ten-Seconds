package com.gh0u1l5.tenseconds.backend.api

import com.gh0u1l5.tenseconds.backend.api.TaskDecorators.withFailureLog
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

        private fun takeDocument(identityId: String): DocumentReference? {
            return takeCollection()?.document(identityId)
        }

        fun fetch(identityId: String): Task<Identity>? {
            return takeDocument(identityId)
                    ?.get()
                    ?.withFailureLog("FireStore")
                    ?.continueWith { it.result.toObject(Identity::class.java) }
        }

        fun fetchAll(): Task<Map<String, Identity>>? {
            return takeCollection()
                    ?.get()
                    ?.withFailureLog("FireStore")
                    ?.continueWith { task ->
                        task.result.associate { identity ->
                            identity.id to identity.toObject(Identity::class.java)
                        }
                    }
        }

        fun add(identity: Identity): Task<DocumentReference>? {
            return takeCollection()
                    ?.add(identity)
                    ?.withFailureLog("FireStore")
        }

        fun update(identityId: String, data: Map<String, Any>): Task<Void>? {
            return takeDocument(identityId)
                    ?.set(data, SetOptions.merge())
                    ?.withFailureLog("FireStore")
        }

        fun delete(identityId: String): Task<Void>? {
            return takeDocument(identityId)
                    ?.delete()
                    ?.withFailureLog("FireStore")
        }
    }

    object AccountCollection {
        private fun takeCollection(identityId: String): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("accounts/${user.uid}/$identityId/")
        }

        private fun takeDocument(identityId: String, accountId: String): DocumentReference? {
            return takeCollection(identityId)?.document(accountId)
        }

        fun fetch(identityId: String, accountId: String): Task<Account>? {
            return takeDocument(identityId, accountId)
                    ?.get()
                    ?.withFailureLog("FireStore")
                    ?.continueWith { it.result.toObject(Account::class.java) }
        }

        fun fetchAll(identityId: String): Task<Map<String, Account>>? {
            return takeCollection(identityId)
                    ?.get()
                    ?.withFailureLog("FireStore")
                    ?.continueWith { task ->
                        task.result.associate { account ->
                            account.id to account.toObject(Account::class.java)
                        }
                    }
        }

        fun add(identityId: String, account: Account): Task<DocumentReference>? {
            return takeCollection(identityId)
                    ?.add(account)
                    ?.withFailureLog("FireStore")
        }

        fun update(identityId: String, accountId: String, data: Map<String, Any>): Task<Void>? {
            return takeDocument(identityId, accountId)
                    ?.set(data, SetOptions.merge())
                    ?.withFailureLog("FireStore")
        }

        fun delete(identityId: String, accountId: String): Task<Void>? {
            return takeDocument(identityId, accountId)
                    ?.delete()
                    ?.withFailureLog("FireStore")
        }
    }
}