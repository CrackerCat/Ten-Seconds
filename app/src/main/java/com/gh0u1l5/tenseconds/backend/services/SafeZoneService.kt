package com.gh0u1l5.tenseconds.backend.services

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE
import android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT
import com.gh0u1l5.tenseconds.BuildConfig
import com.gh0u1l5.tenseconds.backend.crypto.EraseUtils.erase
import com.gh0u1l5.tenseconds.global.Constants.PASSWORD_SURVIVE_INTERVAL
import java.nio.CharBuffer

class SafeZoneService : AccessibilityService() {
    companion object {
        private val cleaner = Runnable {
            password?.apply { password = null; erase() }
        }
        private val handler = Handler(HandlerThread("cleaner").run { start(); looper })

        @Volatile private var password: CharArray? = null

        fun notify(password: CharArray) {
            cleaner.run()
            handler.removeCallbacks(cleaner)

            this.password = password
            handler.postDelayed(cleaner, PASSWORD_SURVIVE_INTERVAL)
        }
    }

    override fun onInterrupt() {
        /* Ignore */
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (password == null) {
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            val source = event.source ?: return
            try {
                source.performAction(ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, CharBuffer.wrap(password))
                })
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w(javaClass.simpleName, e)
                }
            } finally {
                source.recycle()
            }
        }
    }
}