package com.localvoicejournal.mobile.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigHelper {

    private const val KEY_MIN_VERSION = "min_version_code"
    
    fun fetchAndActivate(onComplete: () -> Unit = {}) {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            KEY_MIN_VERSION to 1
        ))
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            onComplete()
        }
    }

    fun getMinVersionCode(): Int {
        return Firebase.remoteConfig.getLong(KEY_MIN_VERSION).toInt()
    }
}
