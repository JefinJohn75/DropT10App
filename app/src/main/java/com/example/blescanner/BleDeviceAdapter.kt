package com.example.blescanner

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blescanner.ble.BleScannerScanResult
import com.example.blescanner.ble.Utils

class BleDeviceAdapter(
      private val onConnect    : (macAddress: String) -> Unit,
      private val onDisconnect    : (macAddress: String) -> Unit,
      private val onOTA    : (macAddress: String) -> Unit
    )    : ListAdapter<BleScannerScanResult, BleDeviceAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<BleScannerScanResult>() {

        override fun areItemsTheSame(
            old: BleScannerScanResult,
            new: BleScannerScanResult
        ): Boolean = old.device.address == new.device.address

        override fun areContentsTheSame(
            old: BleScannerScanResult,
            new: BleScannerScanResult
        ): Boolean = old.device.address == new.device.address &&
                     old.isConnected == new.isConnected       &&
                     old.fwVersion == new.fwVersion

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName    : TextView = view.findViewById(R.id.deviceName)
        val tvAddress : TextView = view.findViewById(R.id.deviceAddress)
        val tvRSSI    : TextView = view.findViewById(R.id.deviceRssi)
        val tvScanData: TextView = view.findViewById(R.id.scan_data)
        val tvVersion   :  TextView = view.findViewById(R.id.tvVersion)
        val btnConnect   : TextView = view.findViewById(R.id.btn_connect)
        val btnOTA   : TextView = view.findViewById(R.id.btn_ota)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ble_device, parent, false)
        return ViewHolder(view)
    }



    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        val deviceName = item.result.scanRecord?.deviceName ?: item.device.name ?: "DropT1"
        holder.tvName.text     = deviceName
        holder.tvAddress.text  = "MAC: ${item.device.address}"

        if (item.isConnected && item.fwVersion.isNotEmpty()) {
            holder.tvRSSI.text = "FW: ${item.fwVersion}"
            holder.tvVersion.visibility = View.GONE
        } else {
            holder.tvRSSI.text = "Signal: ${item.result.rssi} dBm"
            holder.tvVersion.visibility = View.GONE
        }

        holder.tvScanData.text = "Data: ${Utils.bytesToHex(item.scanData)}"

        holder.btnConnect.text = if (item.isConnected) "Disconnect" else "Connect"
        holder.btnConnect.setOnClickListener {
            if (item.isConnected) {
                item.isConnected = false
                notifyItemChanged(position)
                onDisconnect(item.device.address)
            } else {
                item.isConnected = true
                notifyItemChanged(position)
                onConnect(item.device.address)
            }
        }

        holder.btnOTA.setOnClickListener {
            onOTA(item.device.address)
        }

    }


    fun setConnectedState(mac: String, connected: Boolean) {
        val index = currentList.indexOfFirst { it.device.address == mac }
        if(index != -1) {
            currentList[index].isConnected = connected
            notifyItemChanged(index)
        }
    }
}







