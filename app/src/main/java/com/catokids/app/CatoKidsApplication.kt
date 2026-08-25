package com.catokids.app

import android.app.Application
import com.catokids.app.core.AppContainer

class CatoKidsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
