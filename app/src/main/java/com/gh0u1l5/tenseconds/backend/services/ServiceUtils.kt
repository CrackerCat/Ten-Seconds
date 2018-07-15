package com.gh0u1l5.tenseconds.backend.services

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.gh0u1l5.tenseconds.R

object ServiceUtils {
    private val sSafeZoneServiceName = "com.gh0u1l5.tenseconds/${SafeZoneService::class.java.canonicalName}"

    fun isSafeZoneServiceEnabled(context: Context): Boolean {
        val resolver = context.contentResolver
        val enabled = Settings.Secure.getInt(resolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        if (enabled == 1) {
            val list = Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return list.split(':').any { it ==  sSafeZoneServiceName }
        }
        return false
    }

    fun activateSafeZoneService(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
        Toast.makeText(context, R.string.prompt_enable_safe_zone_service, Toast.LENGTH_LONG).show()
    }
}