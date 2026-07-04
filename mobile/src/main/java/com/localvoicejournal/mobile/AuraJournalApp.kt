package com.localvoicejournal.mobile

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.localvoicejournal.mobile.util.RemoteConfigHelper

class AuraJournalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob & Preload Interstitials
        com.localvoicejournal.mobile.util.AdsHelper.initialize(this)
        
        // Initialize Remote Config
        RemoteConfigHelper.fetchAndActivate()
    }
}
