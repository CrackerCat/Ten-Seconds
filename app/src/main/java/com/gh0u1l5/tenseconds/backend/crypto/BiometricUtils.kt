package com.gh0u1l5.tenseconds.backend.crypto

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.AsyncTask
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.global.TenSecondsApplication
import javax.crypto.Cipher

object BiometricUtils {
    private val application by lazy { TenSecondsApplication.instance }

    fun isHardwareAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= 28) {
            val pm = application.getSystemService(PackageManager::class.java)
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        } else {
            val fm = application.getSystemService(FingerprintManager::class.java)
            fm.isHardwareDetected
        }
    }

    fun hasEnrolledFingerprints(): Boolean {
        return if (Build.VERSION.SDK_INT >= 28) {
            // TODO: handle this correctly
            return true
        } else {
            val fm = application.getSystemService(FingerprintManager::class.java)
            fm.hasEnrolledFingerprints()
        }
    }

    fun authenticate(context: Context, cipher: Cipher, success: (Cipher) -> Unit) {
        if (Build.VERSION.SDK_INT >= 28) {
            val cancel = context.getString(R.string.button_cancel)
            val cancelSignal = CancellationSignal()
            val crypto = BiometricPrompt.CryptoObject(cipher)
            val executor = AsyncTask.THREAD_POOL_EXECUTOR
            BiometricPrompt.Builder(context)
                    .setTitle(context.getString(R.string.biometric_prompt_title))
                    .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
                    .setDescription(context.getString(R.string.biometric_prompt_description))
                    .setNegativeButton(cancel, executor, DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        cancelSignal.cancel()
                    })
                    .build()
                    .authenticate(crypto, cancelSignal, executor, object : BiometricPrompt.AuthenticationCallback() {
                        @RequiresApi(28)
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            success(result.cryptoObject.cipher)
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                                Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                            }
                        }
                    })
        } else {
            val cancel = context.getString(R.string.button_cancel)
            val cancelSignal = CancellationSignal()
            val dialog = AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_fingerprint)
                    .setTitle(R.string.biometric_prompt_title)
                    .setMessage(R.string.biometric_prompt_description)
                    .setNegativeButton(cancel) { dialog, _ ->
                        dialog.dismiss()
                        cancelSignal.cancel()
                    }
                    .show()
            val fm = context.getSystemService(FingerprintManager::class.java)
            val crypto = FingerprintManager.CryptoObject(cipher)
            fm.authenticate(crypto, cancelSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    dialog.dismiss()
                    success(result.cryptoObject.cipher)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    if (errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                        dialog.dismiss()
                        Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                    }
                }
            }, null)
        }
    }
}