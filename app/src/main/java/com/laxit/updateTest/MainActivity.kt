package com.laxit.updateTest

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DownloadScreen(FileDownloadViewModel())

        }
    }


    @Composable
    fun DownloadScreen(viewModel: FileDownloadViewModel) {
        FileDownloadScreen(viewModel)
    }


    @Composable
    fun FileDownloadScreen(viewModel: FileDownloadViewModel) {
        val context = LocalContext.current
        val url = "https://ptiadmin.multitvsolution.com/test-apk/test-apk.apk"
        val fileName = "test.apk"

        val scope = rememberCoroutineScope()

        LaunchedEffect(url) {
            viewModel.downloadFile(context, url, fileName)
            viewModel.observeDownloadProgress(context)
        }


        val progress = viewModel.downloadProgress.value

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            Text(text = "Downloading... $progress%",
                color = Color.Black,
                modifier = Modifier.padding(16.dp))

        }
        var file by remember {
            mutableStateOf("")
        }

        if (progress == 100) {
            file = viewModel.getDownloadedFilePath().toString()
            val file = File("/storage/emulated/0/Download/test.apk")
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            } else {
                Uri.fromFile(file)
            }

            Text(text = uri.toString(), color = Color.Black)

            if (Build.VERSION.SDK_INT >= 29) {
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = uri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } else {
               /* val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                   // type = "application/vnd.android.package-archive"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)*/
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = uri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }

    }
}

