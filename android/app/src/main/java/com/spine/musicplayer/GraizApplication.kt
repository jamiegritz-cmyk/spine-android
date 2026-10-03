package com.spine.musicplayer

import android.app.Application
import android.util.Log

class GraizApplication : Application() {
    companion object {
        var fatalCrash: Throwable? = null
    }

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            fatalCrash = throwable
            Log.e("GRAIZ_FATAL", "Uncaught crash on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
