@file:Suppress("DEPRECATION")

package com.gh0u1l5.tenseconds.backend.crypto

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.AsyncTask
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.gh0u1l5.tenseconds.R
import javax.crypto.Cipher

object BiometricUtils {
    interface AuthenticationCallback {
        // TODO: handle more results in future
        fun onSuccess(cipher: Cipher)
        fun onNoBiometrics(context: Context, errString: CharSequence?)
        fun onHardwareNotPresent(context: Context, errString: CharSequence?)
    }

    fun authenticate(context: Context, cipher: Cipher, callback: AuthenticationCallback) {
        val cancel = context.getString(R.string.button_cancel)
        val cancelSignal = CancellationSignal()
        if (Build.VERSION.SDK_INT >= 28) {
            val crypto = BiometricPrompt.CryptoObject(cipher)
            val executor = AsyncTask.THREAD_POOL_EXECUTOR
            val prompt = BiometricPrompt.Builder(context)
                    .setTitle(context.getString(R.string.biometric_prompt_title))
                    .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
                    .setDescription(context.getString(R.string.biometric_prompt_description))
                    .setNegativeButton(cancel, executor, DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        cancelSignal.cancel()
                    })
                    .build()
            prompt.authenticate(crypto, cancelSignal, executor, object : BiometricPrompt.AuthenticationCallback() {
                @RequiresApi(28)
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    callback.onSuccess(result.cryptoObject.cipher)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    when (errorCode) {
                        BiometricPrompt.BIOMETRIC_ERROR_CANCELED,
                        BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> return
                        BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> {
                            callback.onNoBiometrics(context, errString)
                        }
                        BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT -> {
                            callback.onHardwareNotPresent(context, errString)
                        }
                        else -> {
                            Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        } else {
            val crypto = FingerprintManager.CryptoObject(cipher)
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
            fm.authenticate(crypto, cancelSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    dialog.dismiss()
                    callback.onSuccess(result.cryptoObject.cipher)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    when (errorCode) {
                        FingerprintManager.FINGERPRINT_ERROR_CANCELED,
                        FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED -> return
                        FingerprintManager.FINGERPRINT_ERROR_NO_FINGERPRINTS -> {
                            callback.onNoBiometrics(context, errString)
                        }
                        FingerprintManager.FINGERPRINT_ERROR_HW_NOT_PRESENT -> {
                            callback.onHardwareNotPresent(context, errString)
                        }
                        else -> {
                            Toast.makeText(context, errString, Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.dismiss()
                }
            }, null)
        }
    }
}