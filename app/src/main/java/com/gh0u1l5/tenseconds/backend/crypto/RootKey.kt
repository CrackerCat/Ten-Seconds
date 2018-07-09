package com.gh0u1l5.tenseconds.backend.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object RootKey {
    private val sKeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore")
    }

    private val sRootKeyGenerator by lazy {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    }

    private val sRootKeySpec = KeyGenParameterSpec.Builder("root",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).run {
        setBlockModes(KeyProperties.BLOCK_MODE_ECB)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

        setUserAuthenticationRequired(true)
        setUserAuthenticationValidityDurationSeconds(-1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setInvalidatedByBiometricEnrollment(false)
        }

        build()
    }

    fun generate(): SecretKey = sRootKeyGenerator.run {
        init(sRootKeySpec)
        generateKey()
    }

    fun take(): SecretKey? {
        val entry = sKeyStore.getEntry("root", null)
        if (entry !is KeyStore.SecretKeyEntry) {
            Log.w(javaClass.simpleName, "Root Key Not Found")
            return null
        }
        return entry.secretKey
    }
}