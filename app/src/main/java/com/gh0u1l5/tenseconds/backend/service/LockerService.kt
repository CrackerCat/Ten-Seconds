package com.gh0u1l5.tenseconds.backend.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE
import android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT
import com.gh0u1l5.tenseconds.BuildConfig

class LockerService : AccessibilityService() {
    // TODO: notify user to activate this service.

    override fun onInterrupt() {
        /* Ignore */
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            val source = event.source ?: return
            try {
                source.performAction(ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "test")
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