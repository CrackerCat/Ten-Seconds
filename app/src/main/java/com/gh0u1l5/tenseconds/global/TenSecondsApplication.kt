package com.gh0u1l5.tenseconds.global

import android.app.Application

class TenSecondsApplication : Application() {
    companion object {
        lateinit var instance: TenSecondsApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}