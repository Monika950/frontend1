package com.example.treasurehuntapp

import android.app.Application
import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    @javax.inject.Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate() {
        super.onCreate()
        kotlinx.coroutines.runBlocking {
            tokenStorage.forceLoadNow()
        }
    }
}
