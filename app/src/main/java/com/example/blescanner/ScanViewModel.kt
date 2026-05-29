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


class ScanViewModel(application: Application) : AndroidViewModel(application) {


    private val _devices = MutableStateFlow<List<BleScannerScanResult>>(emptyList())
    val devices: StateFlow<List<BleScannerScanResult>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()




    fun startScan() {
        _devices.value      = emptyList()
        _isScanning.value   = true


        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.START_SCAN
            putExtra(BleScanService.EXTRA_WITH_TIMER, true)
        }.also { ContextCompat.startForegroundService(getApplication(), it) }
    }

    fun stopScan() {
        _isScanning.value = false

        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.STOP_SCAN
        }.also { getApplication<Application>().startService(it) }
    }


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


    fun startOta(mac: String, filePath: String) {
        Intent(getApplication(), BleScanService::class.java).apply {
            action = BleScanService.START_OTA
            putExtra(BleScanService.EXTRA_MAC_ADDRESS, mac)
            putExtra(BleScanService.EXTRA_OTA_FILE_PATH, filePath)
        }.also { getApplication<Application>().startService(it) }
    }




    fun addOrUpdateDevice(device: BleScannerScanResult) {
        val updated = _devices.value.toMutableList()

        val index = updated.indexOfFirst {
            it.device.address == device.device.address
        }

        if (index == -1) {
            updated.add(device)
        } else {
            updated[index] = device.copy(
                isConnected = updated[index].isConnected,
                fwVersion   = updated[index].fwVersion
            )
        }

        _devices.value = updated
    }


    override fun onCleared() {
        super.onCleared()
        stopScan()
    }




    fun updateDeviceVersion (mac: String, fw: String) {

        val updated = _devices.value.toMutableList()
        val index = updated.indexOfFirst { it.device.address == mac }
        if (index != -1) {

            updated[index] = updated[index].copy(

                fwVersion = fw,
                isConnected = true
            )
            _devices.value = updated
        }
    }

}