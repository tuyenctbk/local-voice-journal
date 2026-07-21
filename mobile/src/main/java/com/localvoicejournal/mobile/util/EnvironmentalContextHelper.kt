package com.localvoicejournal.mobile.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object EnvironmentalContextHelper {
    private const val TAG = "EnvContextHelper"

    data class ContextData(
        val weather: String,
        val location: String
    )

    suspend fun fetchContext(context: Context): ContextData {
        return withContext(Dispatchers.IO) {
            try {
                var lat: Double? = null
                var lon: Double? = null
                var geoSourceName = ""

                // 1. Try GPS Location (if permission granted)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                        val providers = locationManager?.getProviders(true)
                        var bestLoc: Location? = null
                        if (providers != null) {
                            for (provider in providers) {
                                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                                if (bestLoc == null || loc.accuracy < bestLoc.accuracy) {
                                    bestLoc = loc
                                }
                            }
                        }
                        if (bestLoc != null) {
                            lat = bestLoc.latitude
                            lon = bestLoc.longitude
                            geoSourceName = "GPS"
                            Log.d(TAG, "Acquired GPS location: $lat, $lon")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "GPS getLastKnownLocation failed", e)
                    }
                }

                // 2. Try IP Geolocation (if GPS failed/unavailable)
                var ipCity: String? = null
                var ipCountry: String? = null
                if (lat == null || lon == null) {
                    var connection: HttpURLConnection? = null
                    try {
                        connection = URL("https://ipapi.co/json/").openConnection() as HttpURLConnection
                        connection.connectTimeout = 3000
                        connection.readTimeout = 3000
                        connection.setRequestProperty("User-Agent", "AuraJournalAndroidApp/1.0")
                        val responseCode = connection.responseCode
                        if (responseCode == 429) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "IP Geolocation rate limit reached (Free Tier).", Toast.LENGTH_SHORT).show()
                            }
                        }
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        
                        ipCity = "\"city\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                        ipCountry = "\"country_name\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                        lat = "\"latitude\"\\s*:\\s*([0-9.-]+)".toRegex().find(response)?.groupValues?.get(1)?.toDoubleOrNull()
                        lon = "\"longitude\"\\s*:\\s*([0-9.-]+)".toRegex().find(response)?.groupValues?.get(1)?.toDoubleOrNull()
                        geoSourceName = "IP"
                        Log.d(TAG, "Acquired IP location: $ipCity, $ipCountry ($lat, $lon)")
                    } catch (e: Exception) {
                        Log.e(TAG, "IP Geolocation fallback failed", e)
                    } finally {
                        connection?.disconnect()
                    }
                }

                // 3. Resolve Location Name & Weather if coordinates are available
                if (lat != null && lon != null) {
                    var locationName = ""
                    if (geoSourceName == "GPS") {
                        // Reverse geocode via Nominatim
                        var connection: HttpURLConnection? = null
                        try {
                            connection = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json").openConnection() as HttpURLConnection
                            connection.connectTimeout = 3000
                            connection.readTimeout = 3000
                            connection.setRequestProperty("User-Agent", "AuraJournalAndroidApp/1.0")
                            val responseCode = connection.responseCode
                            if (responseCode == 429) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Nominatim geocoding rate limit reached (Free Tier).", Toast.LENGTH_SHORT).show()
                                }
                            }
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            
                            val suburb = "\"suburb\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                            val neighbourhood = "\"neighbourhood\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                            val city = "\"city\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1) ?:
                                       "\"town\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1) ?:
                                       "\"village\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                            val country = "\"country\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)

                            val localPart = neighbourhood ?: suburb
                            locationName = when {
                                localPart != null && city != null -> "$localPart, $city"
                                city != null && country != null -> "$city, $country"
                                city != null -> city
                                country != null -> country
                                else -> "Nearby Coordinates"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Nominatim Reverse Geocode failed", e)
                            locationName = "Lat: %.2f, Lon: %.2f".format(lat, lon)
                        } finally {
                            connection?.disconnect()
                        }
                    } else {
                        // Use IP city/country resolved earlier
                        locationName = when {
                            ipCity != null && ipCountry != null -> "$ipCity, $ipCountry"
                            ipCity != null -> ipCity
                            ipCountry != null -> ipCountry
                            else -> "IP Coordinates"
                        }
                    }

                    // Query Weather via Open-Meteo
                    var weatherDetails = "Unknown Weather"
                    var connection: HttpURLConnection? = null
                    try {
                        connection = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true").openConnection() as HttpURLConnection
                        connection.connectTimeout = 3000
                        connection.readTimeout = 3000
                        val responseCode = connection.responseCode
                        if (responseCode == 429) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Weather service rate limit reached (Free Tier).", Toast.LENGTH_SHORT).show()
                            }
                        }
                        val response = connection.inputStream.bufferedReader().use { it.readText() }

                        val temp = "\"temperature\"\\s*:\\s*([0-9.-]+)".toRegex().find(response)?.groupValues?.get(1)?.toDoubleOrNull()
                        val wcode = "\"weathercode\"\\s*:\\s*([0-9]+)".toRegex().find(response)?.groupValues?.get(1)?.toIntOrNull()

                        if (temp != null && wcode != null) {
                            val description = mapWeatherCode(wcode)
                            weatherDetails = "${temp}°C, $description"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Open-Meteo Weather query failed", e)
                    } finally {
                        connection?.disconnect()
                    }

                    return@withContext ContextData(weatherDetails, locationName)
                }

                // If completely failed/offline:
                return@withContext ContextData("Unknown Weather", "Offline / Location Hidden")
            } catch (e: Exception) {
                Log.e(TAG, "Environmental Context lookup critical error", e)
                return@withContext ContextData("Unknown Weather", "Offline / Location Hidden")
            }
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Cloudy"
        }
    }
}
