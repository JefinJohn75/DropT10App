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
import com.example.blescanner.ble.DeviceTypeIDMapping
import com.example.blescanner.ble.Utils

class BleDeviceAdapter(
//    private val onStartLocate: (macAddress: String) -> Unit,
//    private val onStopLocate: (macAddress: String) -> Unit,
//    private val onStartPair: (macAddress: String) -> Unit
      private val onConnect    : (macAddress: String) -> Unit,
      private val onDisconnect    : (macAddress: String) -> Unit,
      private val onOTA    : (macAddress: String) -> Unit


    )    : ListAdapter<BleScannerScanResult, BleDeviceAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<BleScannerScanResult>() {

        // Two entries are the SAME ROW if they share the same MAC address
        override fun areItemsTheSame(
            old: BleScannerScanResult,
            new: BleScannerScanResult
        ): Boolean = old.device.address == new.device.address

        override fun areContentsTheSame(
            old: BleScannerScanResult,
            new: BleScannerScanResult
        ): Boolean = old.device.address == new.device.address &&
                     old.isConnected == new.isConnected       &&
                     old.hwVersion == new.hwVersion           &&
                     old.swVersion == new.swVersion           &&
                     old.fwVersion == new.fwVersion

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName    : TextView = view.findViewById(R.id.deviceName)
        val tvAddress : TextView = view.findViewById(R.id.deviceAddress)
        val tvRSSI    : TextView = view.findViewById(R.id.deviceRssi)
        val tvScanData: TextView = view.findViewById(R.id.scan_data)
//        val btnLocate : TextView = view.findViewById(R.id.btn_locate)
//        val btnPair   : TextView = view.findViewById(R.id.btn_pair)

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

        val deviceName = item.result.scanRecord?.deviceName ?: item.device.name ?: DeviceTypeIDMapping.getDeviceNameFromScanRecord(item.scanData)
        holder.tvName.text     = deviceName
        holder.tvAddress.text  = "MAC: ${item.device.address}"
//        holder.tvRSSI.text     = "Signal: ${item.result.rssi} dBm"

        if (item.isConnected && item.fwVersion.isNotEmpty()) {
            holder.tvRSSI.text = "FW: ${item.fwVersion}"
            holder.tvVersion.visibility = View.GONE
        } else {
            holder.tvRSSI.text = "Signal: ${item.result.rssi} dBm"
            holder.tvVersion.visibility = View.GONE
        }

        // Convert raw advertisement bytes to readable HEX string
        holder.tvScanData.text = "Data: ${Utils.bytesToHex(item.scanData)}"


//        if (item.isConnected && item.fwVersion.isNotEmpty()) {                                            ///-----FOR WISILICA------------------
//            holder.tvVersion.visibility = View.VISIBLE
//            holder.tvVersion.text = "HW: ${item.hwVersion}  SW: ${item.swVersion}  FW: ${item.fwVersion}"
//        } else {
//            holder.tvVersion.visibility = View.GONE
//        }


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



//        holder.btnLocate.text  = if (item.isTesting) "Locating..." else "Locate"
//
//        holder.btnLocate.setOnClickListener {
//            if (item.isTesting) {
//                item.isTesting = false
//                notifyItemChanged(position)
//                onStopLocate(item.device.address)
//            } else {
//                item.isTesting = true                       //---START LOCATE------------------------------
//                notifyItemChanged(position)
//                onStartLocate(item.device.address)
//            }
//        }
//        holder.btnPair.text = if (item.isPairing) "Pairing..." else "Pair"
//
//        holder.btnPair.setOnClickListener {
//
//            if (!item.isPairing) {
//                item.isPairing = true
//                notifyItemChanged(position)
//                onStartPair(item.device.address)
//            }
//        }


    }

//    fun resetLocatingState() {
//        currentList.forEach { it.isTesting = false }
//        notifyDataSetChanged()
//    }
//
//
//    fun resetPairingState(mac: String) {
//        val index = currentList.indexOfFirst { it.device.address == mac }
//        if(index != -1) {
//            currentList[index].isPairing = false
//            notifyItemChanged(index)
//        }
//
//    }


    fun setConnectedState(mac: String, connected: Boolean) {
        val index = currentList.indexOfFirst { it.device.address == mac }
        if(index != -1) {
            currentList[index].isConnected = connected
            notifyItemChanged(index)
        }
    }
}







