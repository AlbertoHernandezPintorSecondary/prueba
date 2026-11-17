package com.example.pruebatecnica.data.di

import javax.inject.Qualifier

/** Dispatcher for I/O and data work (mapping, cache). Tests inject a test dispatcher instead. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
