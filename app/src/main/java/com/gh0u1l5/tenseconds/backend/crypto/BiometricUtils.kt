package com.gh0u1l5.tenseconds.backend.crypto

import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import com.gh0u1l5.tenseconds.global.TenSecondsApplication

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
}