package com.gh0u1l5.tenseconds.backend.api

import android.util.Log
import com.gh0u1l5.tenseconds.BuildConfig
import com.google.android.gms.tasks.Task

object TaskDecorators {
    fun<T> Task<T>.withFailureLog(tag: String): Task<T> {
        return this.addOnFailureListener {
            if (BuildConfig.DEBUG) {
                Log.w(tag, it)
            }
        }
    }
}