package com.example.pruebatecnica.presentation

import com.example.pruebatecnica.presentation.common.logging.Logger

/** Records warnings so tests can assert what (and whether) the ViewModels logged. */
class FakeLogger : Logger {

    data class Entry(val tag: String, val message: String, val throwable: Throwable?)

    val warnings = mutableListOf<Entry>()

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        warnings += Entry(tag, message, throwable)
    }
}
