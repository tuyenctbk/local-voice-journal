package com.localvoicejournal.mobile.util

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigHelper {

    private const val PREFS_NAME = "aura_remote_config_lkg"
    private const val KEY_MIN_VERSION = "min_version_code"
    
    fun fetchAndActivate(context: Context, onComplete: () -> Unit = {}) {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            KEY_MIN_VERSION to 1
        ))
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fetchedVal = remoteConfig.getLong(KEY_MIN_VERSION).toInt()
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_MIN_VERSION, fetchedVal)
                    .apply()
            }
            onComplete()
        }
    }

    fun getMinVersionCode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lkgValue = prefs.getInt(KEY_MIN_VERSION, 1)
        
        return try {
            val remoteVal = Firebase.remoteConfig.getLong(KEY_MIN_VERSION).toInt()
            // If remoteVal is 1 (the default), but we have a higher LKG, return LKG
            if (remoteVal == 1 && lkgValue > 1) {
                lkgValue
            } else {
                remoteVal
            }
        } catch (e: Exception) {
            lkgValue
        }
    }
}
