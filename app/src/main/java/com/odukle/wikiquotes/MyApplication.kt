package com.odukle.wikiquotes

import android.app.Application
import com.google.android.gms.ads.MobileAds


/** The Application class that manages com.odukle.wikiquotes.AppOpenManager.  */
class MyApplication : Application() {

    private lateinit var appOpenManager: AppOpenManager

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(
            this
        ) { }

        appOpenManager = AppOpenManager(this)
    }
}