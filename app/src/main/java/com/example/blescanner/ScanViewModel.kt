package com.example.blescanner


import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.blescanner.ble.BleScanService
import com.example.blescanner.ble.BleScannerScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.Mac


class ScanViewModel(application: Application) : AndroidViewModel(application) {


    private val _devices = MutableStateFlow<List<BleScannerScanResult>>(emptyList())
    val devices: StateFlow<List<BleScannerScanResult>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()



    // ── Service control ───────────────────────────────────────────────────────

    fun startScan() {
        _devices.value      = emptyList()   // Clear previous scan results
        _isScanning.value   = true


        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.START_SCAN
            putExtra(BleScanService.EXTRA_WITH_TIMER, true) // Enable 60s auto-stop
        }.also { ContextCompat.startForegroundService(getApplication(), it) }
    }

    fun stopScan() {
        _isScanning.value = false
                                                                                                        // Send ACTION_STOP_SCAN to BleScanService
        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.STOP_SCAN
        }.also { getApplication<Application>().startService(it) }
    }


//
//    fun startLocate(macAddress: String) {
//        Intent(getApplication(), BleScanService::class.java).apply {
//            action = BleScanService.START_LOCATE
//            putExtra(BleScanService.EXTRA_MAC_ADDRESS, macAddress)
//        }.also { getApplication<Application>().startService(it) }
//    }
//
//    fun stopLocate(macAddress: String) {
//        Intent(getApplication(), BleScanService::class.java).apply {
//            action = BleScanService.STOP_LOCATE
//        }.also { getApplication<Application>().startService(it) }
//    }
//
//
//    //---------------------PAIRING FUNCTION-------------------------------------------------------------
//    fun startPairing(mac: String) {
//
//        Intent(getApplication(), BleScanService::class.java).apply {
//            action = BleScanService.START_PAIR
//            putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
//        }.also { getApplication<Application>().startService(it) }
//    }
//
//
//    fun stopPairing() {
//
//        Intent(getApplication(), BleScanService::class.java).apply {
//            action = BleScanService.STOP_PAIR
//        }.also { getApplication<Application>().startService(it) }
//    }
//
//
//



    fun startConnect(mac: String) {
        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.START_CONNECT
            putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
        }.also { getApplication<Application>().startService(it) }
    }


    fun stopConnect(mac: String) {
        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.STOP_CONNECT
        }.also { getApplication<Application>().startService(it) }
    }


    fun startOta(mac: String, filePath: String,uuid:String) {
        // Sends START_OTA to BleScanService
        // → OTA logic will be added here later
        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.START_OTA
            putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
            putExtra(BleScanService.EXTRA_OTA_FILE_PATH, filePath)
            putExtra(BleScanService.EXTRA_OTA_DEVICE_UUID,uuid)
        }.also { getApplication<Application>().startService(it) }
    }














    // ── Data handlers called by ScanFragment's BroadcastReceiver ─────────────

    // Called when ACTION_SCAN_RESULT is received
    // Mirrors StickSense viewModel.processScanResult()
    fun addOrUpdateDevice(device: BleScannerScanResult) {
        val updated = _devices.value.toMutableList()

        val index = updated.indexOfFirst {
            it.device.address == device.device.address  // Match by MAC address
        }

        if (index == -1) {
            updated.add(device)         // New device → add to end of list
        } else {
            updated[index] = device.copy(
                isConnected = updated[index].isConnected,
                hwVersion   = updated[index].hwVersion,
                swVersion   = updated[index].swVersion,
                fwVersion   = updated[index].fwVersion
            )
        }

        _devices.value = updated        // Publish updated list to Fragment
    }



    // Auto-stop service when ViewModel is cleared (app killed)
    override fun onCleared() {
        super.onCleared()
        stopScan()
    }




    fun updateDeviceVersion (mac: String, hw: String, sw: String, fw: String) {

        val updated = _devices.value.toMutableList()
        val index = updated.indexOfFirst { it.device.address == mac }
        if (index != -1) {

            updated[index] = updated[index].copy(
                hwVersion = hw,
                swVersion = sw,
                fwVersion = fw,
                isConnected = true
            )
            _devices.value = updated
        }
    }









}