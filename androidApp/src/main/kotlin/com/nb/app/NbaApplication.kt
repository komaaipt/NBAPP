package com.nb.app

import android.app.Application
import com.nb.app.core.di.initKoin

class NbaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
