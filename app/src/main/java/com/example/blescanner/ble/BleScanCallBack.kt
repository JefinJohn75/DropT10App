package com.example.blescanner.ble

interface BleScanCallBack  {

    fun onScanResult(bleScannerScanResult: BleScannerScanResult)

    fun onError(errorcode: Int)

    fun onScanFinish()

    fun onScanStart()
}
