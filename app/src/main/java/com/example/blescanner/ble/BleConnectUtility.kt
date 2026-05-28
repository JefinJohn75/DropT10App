package com.example.blescanner.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import java.lang.reflect.Method


object BleConnectUtility {


    private var timeoutHandler: android.os.Handler? = null
    private var timeoutRunnable: Runnable? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(
        context: Context,
        device: BluetoothDevice,
        mGattCallback: BluetoothGattCallback
    ): BluetoothGatt? {

        var connectedGatt: BluetoothGatt? = null

        if (Build.VERSION.SDK_INT >= 26) {
            connectedGatt = device.connectGatt(context, false, mGattCallback, 2, 3)

            connectedGatt?.requestConnectionPriority(1)
        } else if (Build.VERSION.SDK_INT >= 23) {
            connectedGatt = device.connectGatt(context, false, mGattCallback, 2)
        } else if (Build.VERSION.SDK_INT >= 21) {
            connectedGatt = connectedGattApi21(context, device, mGattCallback)
            connectedGatt?.requestConnectionPriority(1)
        } else {
            connectedGatt = device.connectGatt(context, false, mGattCallback)
        }
        return connectedGatt
    }

    fun cancelConnectionTimeout() {
        timeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        timeoutHandler = null
        timeoutRunnable = null
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectedGattApi21(
        context: Context,
        device: BluetoothDevice,
        mGattCallback: BluetoothGattCallback
    ): BluetoothGatt? {

        try {
            val connectGatt: Method? = device.javaClass.getMethod(
                "connectGatt",
                Context::class.java,
                Boolean::class.java,
                BluetoothGattCallback::class.java,
                Int::class.javaPrimitiveType
            )

            if (connectGatt != null) {
                return connectGatt.invoke(device, context, false, mGattCallback, 2) as BluetoothGatt
            }
        } catch (e: Exception) {
            Log.w(TAG, "connectGattApi21: $e")
        }
        return device.connectGatt(context, false, mGattCallback)
    }
}