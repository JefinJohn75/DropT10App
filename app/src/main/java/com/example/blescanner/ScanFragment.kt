package com.example.blescanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blescanner.ble.BleScanService
import com.example.blescanner.ble.BleScannerScanResult
import com.example.blescanner.ble.Utils
import kotlinx.coroutines.launch

class ScanFragment : Fragment() {

    private val tag = "ScanFragment"
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter   : BleDeviceAdapter
    private lateinit var btnScan         : Button
    private lateinit var tvStatus        : TextView
    private var pendingOtaMac = ""
    private val OTA_FILE_REQUEST_CODE = 35



    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.startScan()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissions denied — cannot scan.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private val Receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                BleScanService.ACTION_SCAN_RESULT -> {
                    val device: BleScannerScanResult? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BleScanService.EXTRA_BLE_DEVICE,
                                BleScannerScanResult::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BleScanService.EXTRA_BLE_DEVICE)
                        }
                    device?.let {
                        viewModel.addOrUpdateDevice(it)
                        Log.i(tag, "Device received: ${it.device.address}")
                    }
                }




                BleScanService.ACTION_CONNECT_SUCCESS -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?: ""

                    val dropT1Version = intent.getStringExtra(BleScanService.EXTRA_DROP_T1_VERSION)

                    if (dropT1Version != null) {

                        Log.i(tag,"DropT1 Connected: mac= $mac version = $dropT1Version")
                        Toast.makeText(requireContext(),
                            "Connected: $mac\nFW: $dropT1Version",
                            Toast.LENGTH_SHORT).show()
                        deviceAdapter.setConnectedState(mac,true)
                        viewModel.updateDeviceVersion(mac,dropT1Version)
                    } else {

                        deviceAdapter.setConnectedState(mac,true)
                    }

//
                }

                BleScanService.ACTION_CONNECT_FAILED -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?: ""
                    val error = intent.getStringExtra(BleScanService.EXTRA_CONNECT_ERROR) ?: ""
                    Log.i(tag,"Connect failed: mac= $mac error = $error")
                    Toast.makeText(requireContext(), "Connect failed: $mac error = $error", Toast.LENGTH_SHORT).show()
                    deviceAdapter.setConnectedState(mac,false)
                }

                BleScanService.ACTION_CONNECT_STOPPED -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?: ""
                    Log.i(tag,"Disconnected: mac= $mac")
                    Toast.makeText(requireContext(), "Disconnected: $mac", Toast.LENGTH_SHORT).show()
                    deviceAdapter.setConnectedState(mac,false)
                }

                BleScanService.ACTION_OTA_PROGRESS -> {
                    val progress = intent.getFloatExtra(
                        BleScanService.EXTRA_OTA_PROGRESS,0f)

                    Log.i(tag,"OTA progress: ${"%.0f".format(progress)}%")
                    Toast.makeText(context, "OTA: ${"%.0f".format(progress)}%", Toast.LENGTH_SHORT).show()
                }

                BleScanService.ACTION_OTA_COMPLETE -> {

                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?:""
                    Log.i(tag,"OTA complete: $mac")
                    Toast.makeText(requireContext(), "OTA Complete!",Toast.LENGTH_LONG).show()
                    deviceAdapter.setConnectedState(mac,false)
                }

                BleScanService.ACTION_OTA_FAILED -> {
                    val error = intent.getStringExtra (BleScanService.EXTRA_OTA_ERROR) ?:""
                    Log.e(tag,"OTA failed: $error")
                    Toast.makeText(requireContext(), "OTA failed: $error",Toast.LENGTH_LONG).show()
                }



                


            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_scan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnScan  = view.findViewById(R.id.btnScan)
        tvStatus = view.findViewById(R.id.tvStatus)

        val manager = requireContext()
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter

        deviceAdapter = BleDeviceAdapter(
            onConnect        = { macAddress ->        viewModel.startConnect(macAddress) },
            onDisconnect     = { macAddress ->    viewModel.stopConnect(macAddress) },
            onOTA            = { macAddress ->    pendingOtaMac = macAddress
                                                        openStorageAccess()
                                                        }
        )
        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = deviceAdapter
        }

        tvStatus.text = "Press button to scan…"

        btnScan.setOnClickListener {
            if (viewModel.isScanning.value) {
                viewModel.stopScan()
            } else {
                checkBluetoothAndScan()
            }
        }

        observeViewModel()
    }


    override fun onResume() {
        super.onResume()
        registerScanReceiver()
        Log.i(tag, "onResume — receiver registered, waiting for button tap")
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(Receiver)
        Log.i(tag, "onPause — receiver unregistered")
    }

    private fun checkBluetoothAndScan() {
        if (!Utils.isBleEnabled()) {
            tvStatus.text = "Bluetooth is disabled. Please enable it."
            Toast.makeText(
                requireContext(),
                "Please enable Bluetooth first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        checkPermissionsAndScan()
    }

    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.startScan()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun registerScanReceiver() {
        val filter = IntentFilter().apply {
            addAction(BleScanService.ACTION_SCAN_RESULT)
            addAction(BleScanService.ACTION_CONNECT_SUCCESS)
            addAction(BleScanService.ACTION_CONNECT_FAILED)
            addAction(BleScanService.ACTION_CONNECT_STOPPED)
            addAction(BleScanService.ACTION_OTA_PROGRESS)
            addAction(BleScanService.ACTION_OTA_COMPLETE)
            addAction(BleScanService.ACTION_OTA_FAILED)
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(Receiver, filter)
        Log.i(tag, "BroadcastReceiver registered")

    }










    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.devices.collect { list ->
                        deviceAdapter.submitList(list.toList())
                    }
                }

                launch {
                    viewModel.isScanning.collect { scanning ->
                        btnScan.text = if (scanning) "Stop Scan" else "Scan BLE Devices"
                    }
                }

                launch {
                    viewModel.devices.collect { list ->
                        tvStatus.text = when {
                            viewModel.isScanning.value && list.isEmpty() ->
                                "Scanning…"

                            viewModel.isScanning.value ->
                                "Scanning… ${list.size} device(s) found"

                            list.isEmpty() ->
                                "Press button to scan…"

                            else ->
                                "Found ${list.size} device(s). Tap to scan again."
                        }
                    }
                }

            }
        }
    }


    private fun openStorageAccess() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, OTA_FILE_REQUEST_CODE)

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OTA_FILE_REQUEST_CODE) {

                val uriFile = data?.data
                    ?: return

                val filePath = copyFileToInternalStorage(
                    uriFile,
                    "ota_file_"
                ) ?: return

                Log.i(tag, "OTA file path: $filePath")
                viewModel.startOta(pendingOtaMac, filePath)
            }
        }
    }



    private fun copyFileToInternalStorage(
        uri: Uri,
        newDirName: String
    ): String? {

        val returnCursor = requireContext()
            .contentResolver
            .query(
                uri,
                arrayOf(
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                ),
                null, null, null
            )

        val nameIndex = returnCursor
            ?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val name = nameIndex
            ?.let { returnCursor?.getString(it) }
        returnCursor?.close()

        val dir = java.io.File(
            requireContext().filesDir.toString() +
                    "/" + newDirName
        )
        if (!dir.exists()) dir.mkdir()

        val output = java.io.File(
            requireContext().filesDir.toString() +
                    "/" + newDirName + "/" + name
        )

        try {
            val inputStream = requireContext()
                .contentResolver
                .openInputStream(uri)

            val outputStream = java.io.FileOutputStream(output)

            var read: Int
            val buffers = ByteArray(1024)
            while (inputStream?.read(buffers)
                    .also { if (it != null) read = it } != -1) {
                outputStream.write(buffers, 0,
                    buffers.size)
            }
            inputStream?.close()
            outputStream.close()

        } catch (e: Exception) {
            Log.e(tag, "copyFileToInternalStorage: ${e.message}")
            return null
        }

        return output.path
    }

}