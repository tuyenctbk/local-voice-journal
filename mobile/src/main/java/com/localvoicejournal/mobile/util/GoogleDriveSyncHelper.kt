package com.localvoicejournal.mobile.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object GoogleDriveSyncHelper {
    private const val TAG = "GoogleDriveSyncHelper"

    suspend fun getBackupFileId(accessToken: String): String? {
        return withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://www.googleapis.com/drive/v3/files?q=name%3D'backup.aura'+and+'appDataFolder'+in+parents&spaces=appDataFolder")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val code = conn.responseCode
                if (code == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val idRegex = "\"id\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = idRegex.find(response)
                    return@withContext match?.groupValues?.get(1)
                } else {
                    Log.e(TAG, "Search files failed with code $code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching backup file", e)
            } finally {
                conn?.disconnect()
            }
            null
        }
    }

    suspend fun downloadBackupContent(accessToken: String, fileId: String): String? {
        return withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val code = conn.responseCode
                if (code == 200) {
                    return@withContext conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    Log.e(TAG, "Download file failed with code $code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading backup content", e)
            } finally {
                conn?.disconnect()
            }
            null
        }
    }

    private suspend fun createBackupFile(accessToken: String): String? {
        return withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://www.googleapis.com/drive/v3/files")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val metadata = "{\"name\":\"backup.aura\",\"parents\":[\"appDataFolder\"]}"
                conn.outputStream.use { it.write(metadata.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                if (code == 200 || code == 201) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    return@withContext "\"id\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)?.groupValues?.get(1)
                } else {
                    Log.e(TAG, "Create file metadata failed with code $code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating backup metadata", e)
            } finally {
                conn?.disconnect()
            }
            null
        }
    }

    suspend fun uploadBackupContent(accessToken: String, fileId: String, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PATCH"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.setRequestProperty("Content-Type", "application/octet-stream")
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                conn.outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                if (code == 200) {
                    Log.d(TAG, "Successfully uploaded backup content.")
                    return@withContext true
                } else {
                    Log.e(TAG, "Upload content failed with code $code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading backup content", e)
            } finally {
                conn?.disconnect()
            }
            false
        }
    }

    suspend fun uploadBackup(accessToken: String, content: String): Boolean {
        var fileId = getBackupFileId(accessToken)
        if (fileId == null) {
            fileId = createBackupFile(accessToken)
        }
        return if (fileId != null) {
            uploadBackupContent(accessToken, fileId, content)
        } else {
            false
        }
    }
}
