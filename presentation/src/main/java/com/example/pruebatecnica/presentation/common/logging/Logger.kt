package com.example.pruebatecnica.presentation.common.logging

import android.util.Log
import javax.inject.Inject

/**
 * Logging abstraction used by the ViewModels. Keeps them free of `android.util.Log`, so
 * their unit tests run on the plain JVM and can assert what was logged (with a fake).
 */
interface Logger {
    fun warn(tag: String, message: String, throwable: Throwable? = null)
}

/** Production implementation: writes to Logcat. */
class AndroidLogger @Inject constructor() : Logger {
    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }
}
