package com.example.blescanner.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import org.xml.sax.ErrorHandler
import java.security.SecureRandom
import java.util.UUID
import java.util.logging.Logger


@SuppressLint("NewApi")
object BleUtilis {
    private const val TAG = "WiSe SDK : BleUtilis"



    fun getUUID(scanRecord: ByteArray): String {
        val sb = StringBuilder()
        for (i in 9..24) {
            sb.append(String.format("%02x", scanRecord[i].toInt() and 0xff))
        }
        return sb.toString()
    }

    fun getUUIDBytes(scanRecord: ByteArray?): ByteArray? {
        if (scanRecord == null || scanRecord.size < 24) {
            return null
        }
        val sb = ByteArray(16)
        for (i in 9..24) {
            sb[i - 9] = scanRecord[i]
        }
        return sb
    }

    fun getDeviceUUID(scanRecord: ByteArray): ByteArray {
        val uuid = ByteArray(16)
        for (i in 9..24) {
            uuid[i - 9] = scanRecord[i]
        }
        return uuid
    }


    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()


    fun bytesToUuid(bytes: ByteArray): UUID? {
        if (bytes == null) {
            return null
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }

        val hex = String(hexChars)

        return UUID.fromString(
            hex.substring(0, 8) + "-" +
                    hex.substring(8, 12) + "-" +
                    hex.substring(12, 16) + "-" +
                    hex.substring(16, 20) + "-" +
                    hex.substring(20, 32)
        )
    }

    fun getFromDeviceuuid(uuid: String): UUID {
        return UUID.fromString(
            uuid.substring(0, 8) + "-" +
                    uuid.substring(8, 12) + "-" +
                    uuid.substring(12, 16) + "-" +
                    uuid.substring(16, 20) + "-" +
                    uuid.substring(20, 32)
        )
    }


    fun hexStringToByteArray(s: String?): ByteArray? {
        if (s == null || s.length < 32) {
            return null
        }
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s.get(i).digitToIntOrNull(16) ?: -1 shl 4)
            + s.get(i + 1).digitToIntOrNull(16)!! ?: -1).toByte()
            i += 2
        }
        return data
    }





}