package com.example.blescanner.ble


import android.bluetooth.BluetoothAdapter
import android.util.Log


object Utils {

    val hexArray: CharArray = "0123456789ABCDEF".toCharArray()


    fun bytesToHex(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return ""

        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2]     = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun printByteData(dataBytes: ByteArray?): String {
        if (dataBytes == null || dataBytes.isEmpty()) return ""
        val sb = StringBuilder()
        for (b in dataBytes) {
            sb.append(String.format("%02X", b.toInt() and 0xff))
            sb.append(" | ")
        }
        return sb.toString()
    }

    fun isBleEnabled(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return when {
            adapter == null -> {
                // Device has no Bluetooth hardware at all
                Log.d("Utils", "Bluetooth not supported on this device")
                false
            }
            adapter.isEnabled -> {
                // Bluetooth is ON and ready
                Log.d("Utils", "Bluetooth is enabled")
                true
            }
            else -> {
                // Bluetooth hardware exists but is turned OFF
                Log.d("Utils", "Bluetooth is disabled")
                false
            }
        }
    }
}