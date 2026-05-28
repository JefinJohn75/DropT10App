package com.example.blescanner.ble

import android.Manifest
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
import androidx.annotation.RequiresPermission
import java.util.UUID

class BleScanningUtility(var context: Context) {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    public var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
//    private val thermometerServiceUuid =
//        ParcelUuid.fromString("00001809-0000-1000-8000-00805F9B34FB")
    private val enableBt: Int = 10001
    private lateinit var bleScanCallback: BleScanCallBack
    private val tag = this.javaClass.simpleName
    private val scanCallBack = object : ScanCallback() {
        @SuppressLint("MissingPermission", "NewApi")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)


//            if(result.device.name.equals("Stick Sense", ignoreCase = true) || result.device.name.equals("StickSense", ignoreCase = true)) {
//                Log.i(
//                    tag,
//                    "onScanResult>>>>>>>>>>>>>>>>>>>> " + " name: " + result.device.name + " address: " + result.device.address
//                )
//                Log.i(tag, "SCAN DATA>>>>>> " + Utils.printByteData(result.scanRecord?.bytes))
//                val scanResult = StickSenseScanResult(
//                    result,
//                    result.scanRecord?.bytes ?: ByteArray(0),
//                    result.device
//                )
//                bleScanCallback.onScanResult(scanResult)
//            }


            //-----FOR WISILICA DEVICE FILTER------------------FOR WISILICA DEVICE FILTER-----FOR WISILICA DEVICE FILTER--------------------------
//            val scanRecord = result.scanRecord?.bytes
//
//            if (!isWisilicaDevice(scanRecord)) return
//
//            val uuid = BleUtilis.getUUID(scanRecord ?: ByteArray(0))
//
//
//            val bleDevice = BleScannerScanResult(
//                result = result,
//                scanData = result.scanRecord?.bytes ?: ByteArray(0),
//                device = result.device,
//                deviceUuid = uuid
//            )
//            Log.i(tag, "Found: ${result.device.name} | ${result.device.address}")
//            bleScanCallback.onScanResult(bleDevice)


            //---------DROP T1 DEVICE---------------DROP T1 DEVICE---------DROP T1 DEVICE--------------------------------

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
        if (scanning)                                       // Stop existing scan before starting new one
            stopBleScan()
        this.bleScanCallback = bleScanCallback              // Save who receives our results
        enableBluetooth()
        scanning = true


        val scanFilter = ScanFilter.Builder()                                                   //-------FILTER FOR BLE DEVICES
            .build()

//        val DROP_T1_SERVICE_UUID = ParcelUuid(
//            UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB")
//        )
//
//        val scanFilter = ScanFilter.Builder()
//            .setServiceUuid(DROP_T1_SERVICE_UUID)
//            .build()

//        val settingsBuilder = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .build()

        val settingsBuilder = ScanSettings.Builder()
        settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        settingsBuilder.setReportDelay(0)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeCodedPhySupported()) {
            settingsBuilder.setLegacy(false)
            settingsBuilder.setPhy(BluetoothDevice.PHY_LE_1M)
        }

        try {
            bluetoothLeScanner.flushPendingScanResults(scanCallBack)                // Clear leftover results from any previous scan session


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

//
//    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
//    private fun isDropT1Device(result: ScanResult): Boolean {
//        // Check by service UUID
//        val serviceUuids = result.scanRecord?.serviceUuids
//        if (!serviceUuids.isNullOrEmpty()) {
//            val target = ParcelUuid(UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB"))
//            if (serviceUuids.contains(target)) return true
//        }
//        // Fallback: check device name
//        return result.device.name?.startsWith("DropT1", ignoreCase = true) == true
//    }


    @SuppressLint("MissingPermission")
    private fun isDropT1Device(result: android.bluetooth.le.ScanResult): Boolean {

        val nameMatch = result.device.name?.startsWith("DropT1", ignoreCase = true) == true                 //-----CHECK / FILTER 1---------------

        val dropT1ServiceUuid = ParcelUuid(UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB"))
        val uuidMatch = result.scanRecord?.serviceUuids?.contains(dropT1ServiceUuid) == true                        //-----CHECK / FILTER 2---------------

        val matched = nameMatch || uuidMatch
        if (matched) {

            Log.d(tag, "DropT1 found: name = ${result.device.name} | nameMatch = $nameMatch | uuidMatch = $uuidMatch | macAddress = ${result.device.address}")
        }
        return matched
    }




    private fun isWisilicaDevice(scanRecord: ByteArray?): Boolean {
        if (scanRecord == null || scanRecord.size < 8) return false

        val manufacturerId = ((scanRecord[6].toInt() and 0xFF) shl 8) or
                (scanRecord[5].toInt() and 0xFF)

        val packetFormat = scanRecord[7].toInt() and 0xFF

        return (manufacturerId == 0x0197 || manufacturerId == 0x9701) &&
                (packetFormat == 0x00 || packetFormat == 0x01)
    }









    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        try {
            if (!bluetoothAdapter.isEnabled) {
                // bluetoothAdapter.enable();
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