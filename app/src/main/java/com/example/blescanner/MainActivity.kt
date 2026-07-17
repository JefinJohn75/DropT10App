package com.example.blescanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blescanner.ble.BleScanService
import com.example.blescanner.ble.DropT1BLE_OTAModule

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DropT1BLE_OTAModule.OTA_REQUEST_CODE
            && resultCode == Activity.RESULT_OK) {

            val uri = data?.data ?: return
            val mac = DropT1BLE_OTAModule.pendingMac

            val originalName = uri.lastPathSegment
                ?.substringAfterLast("/")
                ?.substringBefore("?")
                ?.ifEmpty { "firmware.bin" }
                ?: "firmware.bin"
            val destFile = java.io.File(cacheDir,originalName)

            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val intent = Intent(this, BleScanService::class.java).apply {
                    action = BleScanService.START_OTA
                    putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
                    putExtra(BleScanService.EXTRA_OTA_FILE_PATH, destFile.absolutePath)
                }
                startService(intent)
            } catch (e: Exception) {
                val intent = Intent(this, BleScanService::class.java).apply {
                    action = BleScanService.START_OTA
                    putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
                    putExtra(BleScanService.EXTRA_OTA_FILE_PATH, uri.toString())
                }
                startService(intent)
            }
        }
    }
}

