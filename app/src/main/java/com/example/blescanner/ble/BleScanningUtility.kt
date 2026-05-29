package com.example.blescanner.ble

import android.content.Context
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleScanningUtility(var context: Context) {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    public var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val enableBt: Int = 10001
    private lateinit var bleScanCallback: BleScanCallBack
    private val tag = this.javaClass.simpleName
    private val scanCallBack = object : ScanCallback() {
        @SuppressLint("MissingPermission", "NewApi")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)


            if (!isDropT1Device(result)) return

            val bleDevice = BleScannerScanResult(
                result = result,
                scanData = result.scanRecord?.bytes ?: ByteArray(0),
                device = result.device,
                deviceUuid = ""
            )

            Log.i(tag, "Found ${result.device.name} | ${result.device.address}")
            bleScanCallback.onScanResult(bleDevice)


        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            bleScanCallback.onError(errorCode)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)

        }
    }

    init {
        scanning = false
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission")
    fun startBleScan(bleScanCallback: BleScanCallBack) {
        if (scanning)
            stopBleScan()
        this.bleScanCallback = bleScanCallback
        enableBluetooth()
        scanning = true


        val scanFilter = ScanFilter.Builder()
            .build()


        val settingsBuilder = ScanSettings.Builder()
        settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        settingsBuilder.setReportDelay(0)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeCodedPhySupported()) {
            settingsBuilder.setLegacy(false)
            settingsBuilder.setPhy(BluetoothDevice.PHY_LE_1M)
        }

        try {
            bluetoothLeScanner.flushPendingScanResults(scanCallBack)


            bluetoothLeScanner.startScan(listOf(scanFilter), settingsBuilder.build(), scanCallBack)

            Log.v(
                "BleScanningUtility",
                "SCAN STARTED || SCAN STARTED || SCAN STARTED || SCAN STARTED || SCAN STARTED || SCAN STARTED || SCAN STARTED || " + System.currentTimeMillis()
            )
            bleScanCallback.onScanStart()

        } catch (e: Exception) {
            Log.e(
                "BleScanningUtility",
                "SCAN FAILED || SCAN FAILED || SCAN FAILED || " + e.message
            )
        }

    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        try {
            scanning = false
            bluetoothLeScanner.stopScan(scanCallBack)
            Log.e(
                "BleScanningUtility",
                "SCAN STOPPED || SCAN STOPPED || SCAN STOPPED || " + System.currentTimeMillis()
            )
        } catch (e: java.lang.Exception) {
            e.message?.let {
                Log.e(
                    "BleScanningUtility",
                    it
                )
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun isDropT1Device(result: android.bluetooth.le.ScanResult): Boolean {

        val nameMatch = result.device.name?.startsWith("DropT1", ignoreCase = true) == true

        val dropT1ServiceUuid = ParcelUuid(UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB"))
        val uuidMatch = result.scanRecord?.serviceUuids?.contains(dropT1ServiceUuid) == true

        val matched = nameMatch || uuidMatch
        if (matched) {

            Log.d(tag, "DropT1 found: name = ${result.device.name} | nameMatch = $nameMatch | uuidMatch = $uuidMatch | macAddress = ${result.device.address}")
        }
        return matched
    }


    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        try {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                (context as Activity).startActivityForResult(
                    enableBtIntent,
                    enableBt
                )
            }
        } catch (e: java.lang.Exception) {
            e.message?.let {
                Log.e(
                    "BleScanningUtility",
                    it
                )
            }
        }
    }


}