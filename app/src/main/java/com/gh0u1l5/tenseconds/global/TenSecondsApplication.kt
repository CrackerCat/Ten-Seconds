package com.gh0u1l5.tenseconds.global

import android.app.Application
import java.security.KeyStore

class TenSecondsApplication : Application() {
    companion object {
        lateinit var instance: TenSecondsApplication

        val sAndroidKeyStore: KeyStore by lazy {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}