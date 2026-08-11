package com.oqba26.jafr.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val url: String,
    val releaseNotes: String,
    val isForceUpdate: Boolean = false,
)

class UpdateManager(private val context: Context) {

    private val updateUrl = "https://raw.githubusercontent.com/oqba26/Jafr/master/update.json"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            Log.d("UpdateManager", "Network not available")
            return@withContext null
        }

        try {
            // جلوگیری از کش شدن پاسخ توسط سرور یا پروکسی
            val timestamp = System.currentTimeMillis()
            val urlWithParams = if (updateUrl.contains("?")) "$updateUrl&t=$timestamp" else "$updateUrl?t=$timestamp"

            Log.d("UpdateManager", "Checking for update at: $urlWithParams")
            val response = client.get(urlWithParams)
            Log.d("UpdateManager", "Response status: ${response.status}")

            if (response.status.value !in 200..299) {
                Log.e("UpdateManager", "Update check failed with status: ${response.status}")
                return@withContext null
            }

            val responseText: String = response.bodyAsText()
            Log.d("UpdateManager", "Response body: $responseText")

            val updateInfo: UpdateInfo = json.decodeFromString(responseText)

            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }

            Log.d("UpdateManager", "Current version: $currentVersionCode, Server version: ${updateInfo.versionCode}")

            if (updateInfo.versionCode > currentVersionCode) {
                return@withContext updateInfo
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error checking for update", e)
        }
        null
    }

    fun downloadAndInstall(url: String, fileName: String): Long {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "لطفاً اجازه نصب برنامه‌های ناشناخته را بدهید", Toast.LENGTH_LONG).show()
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = "package:${context.packageName}".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return -1L
                }
            }
        } catch (e: SecurityException) {
            Log.e("UpdateManager", "SecurityException: Missing REQUEST_INSTALL_PACKAGES in manifest", e)
            // اگر پرمیشن در مانیفست نباشد، این خطا رخ می‌دهد. در این حالت سعی می‌کنیم مستقیم ادامه دهیم
            // یا به کاربر اطلاع دهیم که نسخه فعلی مشکل دارد.
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = url.toUri()

        val oldFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (oldFile.exists()) oldFile.delete()

        val request = DownloadManager.Request(uri)
            .setTitle("دریافت به‌روزرسانی جفر")
            .setDescription("نسخه جدید در حال دانلود است...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to enqueue download", e)
            Toast.makeText(context, "خطا در شروع دانلود. باز کردن در مرورگر...", Toast.LENGTH_LONG).show()
            openInBrowser(url)
            return -1L
        }

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            installApk(fileName)
                        } else {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = cursor.getInt(reasonIndex)
                            Log.e("UpdateManager", "Download failed. Reason: $reason")
                            Toast.makeText(context, "دانلود ناموفق بود. باز کردن در مرورگر...", Toast.LENGTH_LONG).show()
                            openInBrowser(url)
                        }
                        cursor.close()
                    }
                    try {
                        receiverContext.unregisterReceiver(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(context, onComplete, filter, ContextCompat.RECEIVER_EXPORTED)

        return downloadId
    }

    fun getDownloadProgress(downloadId: Long): Flow<Float> = flow {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1 && statusIndex != -1) {
                    val bytesDownloaded = cursor.getInt(bytesDownloadedIndex)
                    val bytesTotal = cursor.getInt(bytesTotalIndex)
                    val status = cursor.getInt(statusIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        isDownloading = false
                    }

                    if (bytesTotal > 0) {
                        emit(bytesDownloaded.toFloat() / bytesTotal.toFloat())
                    }
                }
                cursor.close()
            } else {
                isDownloading = false
                cursor?.close()
            }
            if (isDownloading) delay(500.milliseconds)
        }
    }.flowOn(Dispatchers.IO)

    private fun installApk(fileName: String) {
        val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!apkFile.exists()) {
            Toast.makeText(context, "فایل نصب پیدا نشد!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "com.oqba26.jafr.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اجرای فایل نصب", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
