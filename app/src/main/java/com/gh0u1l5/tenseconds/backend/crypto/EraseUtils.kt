package com.gh0u1l5.tenseconds.backend.crypto

import android.util.Log
import javax.crypto.spec.SecretKeySpec

object EraseUtils {
    /**
     * Erases a ByteArray by filling 0
     */
    fun ByteArray.erase() {
        this.fill(0)
    }

    /**
     * Erases a CharArray by filling '\u0000'
     */
    fun CharArray.erase() {
        this.fill('\u0000')
    }

    /**
     * Erases a raw secret key using reflection.
     */
    fun SecretKeySpec.erase() {
        try {
            val keyField = javaClass.getDeclaredField("key").apply {
                isAccessible = true
            }
            (keyField.get(this) as ByteArray).erase()
        } catch (e: Exception) {
            Log.w("EraseUtils", e)
        }
    }
}