package com.example.blescanner.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class BleScannerScanResult(
    val result  : ScanResult,
    val scanData: ByteArray,
    val device  : BluetoothDevice,
    var fwVersion: String = "",
    var isConnected : Boolean = false,
    var deviceUuid : String = ""

) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BleScannerScanResult
        return device.address == other.device.address  &&
                isConnected == other.isConnected &&
                fwVersion == other.fwVersion


    }

    override fun hashCode(): Int {
        return device.address.hashCode()

    }
}