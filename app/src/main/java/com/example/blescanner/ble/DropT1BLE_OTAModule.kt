package com.example.blescanner.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = DropT1BLE_OTAModule.NAME)

class DropT1BLE_OTAModule (
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "DropT1OTA"
        const val OTA_REQUEST_CODE = 9001
        var pendingMac = ""
    }

    override fun getName() = NAME

    private var listenerCount = 0


    @ReactMethod
    fun addListener(eventName: String) {
        if (listenerCount == 0) registerReciever()
        listenerCount ++
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        listenerCount -= count
        if (listenerCount == 0) unregisterReciever()
    }




    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @ReactMethod
    fun startScan() {
        val adapter = (reactContext
            .getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager).adapter

        if (!adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            currentActivity?.startActivityForResult(intent, 1001)
            return
        }
        startService(BleScanService.START_SCAN)
    }

    @ReactMethod
    fun stopScan() {
        startService(BleScanService.STOP_SCAN)
    }

    @ReactMethod
    fun connectDevice(mac:String) {
        startService(BleScanService.START_CONNECT) {
            putExtra(BleScanService.EXTRA_MAC_ADDRESS,mac)
        }
    }

    @ReactMethod
    fun disconnectDevice (mac: String) {
        startService(BleScanService.STOP_CONNECT) {
            putExtra(BleScanService.EXTRA_MAC_ADDRESS,mac)
        }
    }



    @ReactMethod
    fun startOta(mac: String, contentUri: String) {

        val originalName = contentUri
            .substringAfterLast("/")
            .substringBefore("?")
            .ifEmpty { "firmware.bin" }

        val destFile = java.io.File(reactContext.cacheDir, originalName)

        try {
            reactContext.contentResolver
                .openInputStream(android.net.Uri.parse(contentUri))
                ?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

            startService(BleScanService.START_OTA) {
                putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
                putExtra(BleScanService.EXTRA_OTA_FILE_PATH, destFile.absolutePath)
            }

        } catch (e: Exception) {
            startService(BleScanService.START_OTA) {
                putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
                putExtra(BleScanService.EXTRA_OTA_FILE_PATH, contentUri)
            }
        }
    }

    @ReactMethod
    fun openFilePicker(mac: String) {
        pendingMac = mac
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        currentActivity?.startActivityForResult(intent, OTA_REQUEST_CODE)
    }



    private fun startService(action:String, extras: (Intent.() -> Unit)? = null) {
        val intent = Intent(reactContext, BleScanService::class.java).apply {
            this.action = action
            extras?.invoke(this)
        }
        reactContext.startService(intent)
    }




    private fun sendEvent(evenName: String, data: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule
                .RCTDeviceEventEmitter::class.java)
            .emit(evenName, data)
    }




    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {


                BleScanService.ACTION_SCAN_RESULT -> {
                    val device =  intent.getParcelableExtra<BleScannerScanResult>(
                        BleScanService.EXTRA_BLE_DEVICE)
//                    device?.let {
//                        sendEvent("onDeviceFound", it.device.address)
//                    }
                    device?.let {
                        val nameFromDevice = it.device.name
                        val nameFromRecord = it.result.scanRecord?.deviceName
                        val name = nameFromDevice ?: nameFromRecord ?: "DropT1-FFFF"
                        Log.d("DropT1OtaModule", "nameFromDevice=$nameFromDevice nameFromRecord=$nameFromRecord finalName=$name")

                        sendEvent("onDeviceFound", "$name|${it.device.address}")
                    }
                }

                BleScanService.ACTION_CONNECT_SUCCESS -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?:""
                    val version = intent.getStringExtra(BleScanService.EXTRA_DROP_T1_VERSION) ?:""
                    sendEvent("onConnected", "$mac|$version")
                }

                BleScanService.ACTION_CONNECT_STOPPED -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?:""
                    sendEvent("onDisconnected", mac)
                }

                BleScanService.ACTION_OTA_PROGRESS -> {
                    val progress = intent.getFloatExtra(BleScanService.EXTRA_OTA_PROGRESS,0f)
                    sendEvent("onOtaProgress", progress.toInt().toString())
                }

                BleScanService.ACTION_OTA_COMPLETE -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_OTA_ERROR) ?:""
                    sendEvent("onOtaComplete", mac)
                }

                BleScanService.ACTION_OTA_FAILED -> {
                    val reason = intent.getStringExtra(BleScanService.EXTRA_OTA_ERROR) ?:""
                    sendEvent("onOtaFailed", reason)
                }
            }
        }
    }


    private fun registerReciever() {
        val filter = IntentFilter().apply {
            addAction(BleScanService.ACTION_SCAN_RESULT)
            addAction(BleScanService.ACTION_CONNECT_SUCCESS)
            addAction(BleScanService.ACTION_CONNECT_STOPPED)
            addAction(BleScanService.ACTION_OTA_PROGRESS)
            addAction(BleScanService.ACTION_OTA_COMPLETE)
            addAction(BleScanService.ACTION_OTA_FAILED)
        }
        LocalBroadcastManager.getInstance(reactContext)
            .registerReceiver(receiver, filter)
        }


    private fun unregisterReciever() {
        LocalBroadcastManager.getInstance(reactContext)
            .unregisterReceiver(receiver)
        }

}