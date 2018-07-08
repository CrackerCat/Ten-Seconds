package com.gh0u1l5.tenseconds.api

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object Store {
    val instance by lazy {
        FirebaseFirestore.getInstance()
    }

    fun initializeUserData(user: FirebaseUser) {
        instance.collection("users").document(user.uid)
    }
}