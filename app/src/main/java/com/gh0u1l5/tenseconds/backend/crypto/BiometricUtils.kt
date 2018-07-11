package com.gh0u1l5.tenseconds.backend.crypto

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.AsyncTask
import android.os.Build
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.global.TenSecondsApplication
import javax.crypto.Cipher

object BiometricUtils {
    private val context by lazy { TenSecondsApplication.instance }

    fun isHardwareAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= 28) {
            val pm = context.getSystemService(PackageManager::class.java)
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        } else {
            val fm = context.getSystemService(FingerprintManager::class.java)
            fm.isHardwareDetected
        }
    }

    fun hasEnrolledFingerprints(): Boolean {
        return if (Build.VERSION.SDK_INT >= 28) {
            // TODO: handle this correctly
            return true
        } else {
            val fm = context.getSystemService(FingerprintManager::class.java)
            fm.hasEnrolledFingerprints()
        }
    }

    fun authenticate(cipher: Cipher, success: (Cipher) -> Unit) {
        if (Build.VERSION.SDK_INT >= 28) {
            val title = context.getString(R.string.biometric_prompt_title)
            val cancel = context.getString(R.string.button_cancel)
            val crypto = BiometricPrompt.CryptoObject(cipher)
            val executor = AsyncTask.THREAD_POOL_EXECUTOR
            BiometricPrompt.Builder(context)
                    .setTitle(title)
                    .setNegativeButton(cancel, executor, DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                    })
                    .build()
                    .authenticate(crypto, null, executor, object : BiometricPrompt.AuthenticationCallback() {
                        @RequiresApi(28)
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            success(result.cryptoObject.cipher)
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                        }
                    })
        } else {
            // TODO: add a dialog for this one
            val fm = context.getSystemService(FingerprintManager::class.java)
            val crypto = FingerprintManager.CryptoObject(cipher)
            fm.authenticate(crypto, null, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    success(result.cryptoObject.cipher)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                }
            }, null)
        }
    }
}