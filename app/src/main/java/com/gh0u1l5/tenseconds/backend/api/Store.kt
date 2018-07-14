package com.gh0u1l5.tenseconds.backend.api

import com.gh0u1l5.tenseconds.backend.api.TaskDecorators.withFailureLog
import com.gh0u1l5.tenseconds.backend.bean.Account
import com.gh0u1l5.tenseconds.backend.bean.Identity
import com.gh0u1l5.tenseconds.backend.bean.OAuthInfo
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

@Suppress("MemberVisibilityCanBePrivate")
object Store {
    val instance by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build()
        }
    }

    object OAuthCollection {
        private fun takeCollection(): CollectionReference? {
            return instance.collection("oauth/")
        }

        private fun Auth.OAuthType.toDocumentId(): String {
            return when (this) {
                Auth.OAuthType.Facebook -> "Facebook"
                Auth.OAuthType.GitHub   -> "GitHub"
                Auth.OAuthType.Google   -> "Google"
                Auth.OAuthType.Twitter  -> "Twitter"
            }
        }

        fun fetch(type: Auth.OAuthType): Task<OAuthInfo>? {
            return takeCollection()?.document(type.toDocumentId())
                    ?.get()
                    ?.continueWith { it.result.toObject(OAuthInfo::class.java)!! }
                    ?.withFailureLog("FireStore")
        }
    }

    object IdentityCollection {
        private fun takeCollection(): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("users/${user.uid}/identities/")
        }

        private fun takeDocument(identityId: String): DocumentReference? {
            return takeCollection()?.document(identityId)
        }

        fun fetch(identityId: String): Task<Identity>? {
            return takeDocument(identityId)
                    ?.get()
                    ?.continueWith { it.result.toObject(Identity::class.java)!! }
                    ?.withFailureLog("FireStore")
        }

        fun fetchAll(): Task<LinkedHashMap<String, Identity>>? {
            return takeCollection()
                    ?.get()
                    ?.continueWith { task ->
                        LinkedHashMap(task.result.associate { identity ->
                            identity.id to identity.toObject(Identity::class.java)
                        })
                    }
                    ?.withFailureLog("FireStore")
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
            MasterKey.delete(identityId)
            return takeDocument(identityId)
                    ?.delete()
                    ?.withFailureLog("FireStore")
        }
    }

    object AccountCollection {
        private fun takeCollection(identityId: String): CollectionReference? {
            val user = Auth.instance.currentUser ?: return null
            return instance.collection("users/${user.uid}/identities/$identityId/accounts/")
        }

        private fun takeDocument(identityId: String, accountId: String): DocumentReference? {
            return takeCollection(identityId)?.document(accountId)
        }

        fun fetch(identityId: String, accountId: String): Task<Account>? {
            return takeDocument(identityId, accountId)
                    ?.get()
                    ?.continueWith { it.result.toObject(Account::class.java)!! }
                    ?.withFailureLog("FireStore")
        }

        fun fetchAll(identityId: String): Task<LinkedHashMap<String, Account>>? {
            return takeCollection(identityId)
                    ?.get()
                    ?.continueWith { task ->
                        LinkedHashMap(task.result.associate { account ->
                            account.id to account.toObject(Account::class.java)
                        })
                    }
                    ?.withFailureLog("FireStore")
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