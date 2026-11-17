package com.example.pruebatecnica

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt composition root: generates the SingletonComponent with the bindings from every module. */
@HiltAndroidApp
class RickAndMortyApp : Application()
