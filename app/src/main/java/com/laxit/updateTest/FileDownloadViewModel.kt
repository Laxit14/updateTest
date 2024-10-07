package com.laxit.updateTest

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FileDownloadViewModel : ViewModel() {
    private val _downloadProgress = mutableIntStateOf(0)
    val downloadProgress: State<Int> = _downloadProgress

    private var downloadId: Long = -1
    private var downloadFilePath: String? = null


    fun downloadFile(context: Context, url: String, fileName: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading $fileName")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
    }
    fun getDownloadedFilePath(): String? {
        return downloadFilePath
    }

    fun observeDownloadProgress(context: Context) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query().setFilterById(downloadId)

        viewModelScope.launch {
            while (true) {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)

                    val bytesDownloaded = cursor.getInt(bytesDownloadedIndex)
                    val bytesTotal = cursor.getInt(bytesTotalIndex)

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded.toFloat() / bytesTotal.toFloat() * 100).toInt()
                        _downloadProgress.value = progress // Update the state here
                    }

                    val status = cursor.getInt(statusIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        downloadFilePath = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        break
                    }
                }
                cursor.close()
                delay(1000) // Polling interval
            }
        }
    }

}
