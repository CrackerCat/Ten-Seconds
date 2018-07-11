package com.gh0u1l5.tenseconds.backend.crypto

import java.security.KeyStore

object CryptoObjects {
    val sAndroidKeyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }
}