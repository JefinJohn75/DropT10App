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
import com.example.blescanner.R
import com.example.blescanner.ble.BleScanCallBack
import com.example.blescanner.ble.BleScanService
import com.example.blescanner.ble.BleScannerScanResult
import com.example.blescanner.ble.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

import java.io.FileOutputStream                      // needed for FileOutputStream

class ScanFragment : Fragment() {

    private val tag = "ScanFragment"
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter   : BleDeviceAdapter
    private lateinit var btnScan         : Button
    private lateinit var tvStatus        : TextView
    private var pendingOtaMac = ""
    private var pendingOtaUuid = ""

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

                // BleScanService found a device → unpack and push to ViewModel
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
                        viewModel.addOrUpdateDevice(it)     // Add/update in list
                        Log.i(tag, "Device received: ${it.device.address}")
                    }
                }



//                BleScanService.ACTION_LOCATE_STARTED -> {
//                    Log.i(tag, "Locate started----------------------------------")
//                }
//
//                BleScanService.ACTION_LOCATE_STOPPED,
//                BleScanService.ACTION_LOCATE_ERROR -> {
//                    Log.i(tag, "Locate stopped/error-------------------------------------------")
//                    deviceAdapter.resetLocatingState()
//                }
//
//
//                BleScanService.ACTION_PAIRING_STARTED -> {
//                    val mac = intent.getStringExtra(BleScanService.EXTRA_PAIR_MAC) ?: ""
//                    Log.i(tag,"Pairing started: $mac")
//                    Toast.makeText(requireContext(), "Pairing  $mac....", Toast.LENGTH_SHORT).show()
//                }
//
//                BleScanService.ACTION_PAIRING_SUCCESS -> {
//                    val mac = intent.getStringExtra(BleScanService.EXTRA_PAIR_MAC) ?: ""
//                    val meshId = intent.getIntExtra(BleScanService.EXTRA_PAIR_MESH_ID, 0)
//                    Log.i(tag,"Pairing success: mac= $mac meshId = $meshId")
//                    Toast.makeText(requireContext(), "Paired: $mac meshId = $meshId", Toast.LENGTH_SHORT).show()
//                    deviceAdapter.resetPairingState(mac)
//                }
//
//                BleScanService.ACTION_PAIRING_FAILED -> {
//                    val mac = intent.getStringExtra(BleScanService.EXTRA_PAIR_MAC) ?: ""
//                    val error = intent.getStringExtra(BleScanService.EXTRA_PAIR_ERROR) ?: ""
//                    Log.i(tag,"Pairing failed: mac= $mac error = $error")
//                    Toast.makeText(requireContext(), "Pairing failed: $mac error = $error", Toast.LENGTH_SHORT).show()
//                    deviceAdapter.resetPairingState(mac)
//                }


                BleScanService.ACTION_CONNECT_SUCCESS -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?: ""

                    val dropT1Version = intent.getStringExtra(BleScanService.EXTRA_DROP_T1_VERSION)

                    if (dropT1Version != null) {

                        Log.i(tag,"DropT1 Connected: mac= $mac version = $dropT1Version")
                        Toast.makeText(requireContext(),
                            "Connected: $mac\nFW: $dropT1Version",
                            Toast.LENGTH_SHORT).show()
                        deviceAdapter.setConnectedState(mac,true)
                        viewModel.updateDeviceVersion(mac,"","",dropT1Version)
                    } else {

//                    val hw = intent.getStringExtra(BleScanService.EXTRA_HW_VERSION) ?: ""                                 //----FOR WISILICA------to down
//                    val sw = intent.getStringExtra(BleScanService.EXTRA_SW_VERSION) ?: ""
//                    val fw = intent.getStringExtra(BleScanService.EXTRA_FW_VERSION) ?: ""
//
//                    Log.i(tag,"Connected: mac= $mac HW=$hw SW=$sw FW=$fw")
//                    Toast.makeText(requireContext(), "Connected: $mac\nHW=$hw\nSW=$sw\nFW=$fw", Toast.LENGTH_SHORT).show()
//                    deviceAdapter.setConnectedState(mac,true)
//                    viewModel.updateDeviceVersion(mac,hw,sw,fw)
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

//                BleScanService.ACTION_OTA_CLICKED -> {
//                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC_ADDRESS) ?: ""
//                    Log.i(tag, "OTA clicked: $mac")
//                    Toast.makeText(context, "OTA coming soon for $mac", Toast.LENGTH_SHORT).show()
//                    // OTA logic will be added here later
//
//                }

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

    // ── Fragment lifecycle ────────────────────────────────────────────────────

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

        // Setup RecyclerView with our ListAdapter
        deviceAdapter = BleDeviceAdapter(
//            onStartLocate = { macAddress ->    viewModel.startLocate(macAddress) },
//            onStopLocate = { macAddress ->     viewModel.stopLocate(macAddress)  },
//            onStartPair = { macAddress ->     viewModel.startPairing(macAddress) },
            onConnect        = { macAddress ->        viewModel.startConnect(macAddress) },
            onDisconnect     = { macAddress ->    viewModel.stopConnect(macAddress) },
//            onOTA            = { macAddress ->    pendingOtaMac = macAddress
//                                                                    otaFilePicker.launch(                           //----PHONE FILE MANAGER OPENS------------------
//                                                                        "application/octet-stream"
//                                                                    )}
            onOTA            = { macAddress ->    pendingOtaMac = macAddress
                                                  val device = viewModel.devices.value
                                                      .firstOrNull() {it.device.address == macAddress}
                                                      pendingOtaUuid = device?.deviceUuid ?:""

                                                            openStorageAccess()                                      //----PHONE FILE MANAGER OPENS------------------
                                                        }
        )
        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = deviceAdapter
        }

        tvStatus.text = "Press button to scan…"

        btnScan.setOnClickListener {
            if (viewModel.isScanning.value) {
                viewModel.stopScan()                // Scanning → stop it
            } else {
                checkBluetoothAndScan()             // Not scanning → begin flow
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
            return                                  // Stop here — do not proceed
        }
        checkPermissionsAndScan()                   // BT is ON → check permissions
    }

    // ── Step 2b: Check / request runtime permissions ──────────────────────────
    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(                                // Android 12+
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(                                // Android 11 and below
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.startScan()                   // ── STEP 3: Start the scan
        } else {
            permissionLauncher.launch(permissions)  // Ask user for permissions
        }
    }

    // Register BroadcastReceiver to listen for service broadcasts
    private fun registerScanReceiver() {
        val filter = IntentFilter().apply {
            addAction(BleScanService.ACTION_SCAN_RESULT)    // Device found broadcast
//            addAction(BleScanService.ACTION_LOCATE_STARTED)
//            addAction(BleScanService.ACTION_LOCATE_STOPPED)
//            addAction(BleScanService.ACTION_LOCATE_ERROR)
//            addAction(BleScanService.ACTION_PAIRING_STARTED)
//            addAction(BleScanService.ACTION_PAIRING_SUCCESS)
//            addAction(BleScanService.ACTION_PAIRING_FAILED)
            addAction(BleScanService.ACTION_CONNECT_SUCCESS)
            addAction(BleScanService.ACTION_CONNECT_FAILED)
            addAction(BleScanService.ACTION_CONNECT_STOPPED)
//            addAction(BleScanService.ACTION_OTA_CLICKED)
            addAction(BleScanService.ACTION_OTA_PROGRESS)
            addAction(BleScanService.ACTION_OTA_COMPLETE)
            addAction(BleScanService.ACTION_OTA_FAILED)
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(Receiver, filter)
        Log.i(tag, "BroadcastReceiver registered")

    }










    // ── Step 4: Observe ViewModel StateFlows ─────────────────────────────────
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Device list → submit to adapter (DiffUtil handles animation)
                launch {
                    viewModel.devices.collect { list ->
                        deviceAdapter.submitList(list.toList())
                    }
                }

                // Scanning state → toggle button label
                launch {
                    viewModel.isScanning.collect { scanning ->
                        btnScan.text = if (scanning) "Stop Scan" else "Scan BLE Devices"
                    }
                }

                // Status text → live feedback to the user
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

    // Retry/Cancel dialog shown when 60s scan finds nothing






    private fun openStorageAccess() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"                                                    //---for any files like .bin, .zip or any
        intent.addCategory(Intent.CATEGORY_OPENABLE)                //---only shows files that can opened and readable) means not system file or other
        startActivityForResult(intent, OTA_FILE_REQUEST_CODE)    //---launch file manager to select file (35 as racking number s=same as lumos)

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(                              //---called after user picks file from manager----
        requestCode: Int,                                       //---35
        resultCode: Int,
        data: Intent?                                           //---contains the picked file URI(address)-----
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {                 // user picked a file successfully
            if (requestCode == OTA_FILE_REQUEST_CODE) {         // confirm it came from our OTA picker

                val uriFile = data?.data                        //---get the URI of the picked file-----
                    ?: return                                   // if null → user cancelled → stop

                val filePath = copyFileToInternalStorage(       //---copy file to our app storage
                    uriFile,
                    "ota_file_"                            // folder name prefix
                ) ?: return                                     // if copy failed → stop

                Log.i(tag, "OTA file path: $filePath")     // log the path for debugging
                viewModel.startOta(pendingOtaMac, filePath,pendingOtaUuid)      // start OTA with MAC + file path
            }
        }
    }



    private fun copyFileToInternalStorage(
        uri: Uri,                                        //---URI(address) of picked file
        newDirName: String                               //--- name of folder we create to store our copy
    ): String? {

        val returnCursor = requireContext()              // query file info from ContentResolver
            .contentResolver
            .query(
                uri,
                arrayOf(                                 // we want these two columns
                    OpenableColumns.DISPLAY_NAME,        // original file name e.g. firmware.bin
                    OpenableColumns.SIZE                 // file size in bytes
                ),
                null, null, null
            )

        val nameIndex = returnCursor                     // get column index for DISPLAY_NAME
            ?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()                      // move cursor to first (only) row
        val name = nameIndex                             // read the file name
            ?.let { returnCursor?.getString(it) }
        returnCursor?.close()                            // close cursor — good practice

        val dir = java.io.File(                          // create folder in app files directory
            requireContext().filesDir.toString() +
                    "/" + newDirName
        )
        if (!dir.exists()) dir.mkdir()                   // create folder if it doesn't exist

        val output = java.io.File(                       // final file path for our copy
            requireContext().filesDir.toString() +
                    "/" + newDirName + "/" + name                // e.g. /files/ota_file_/firmware.bin
        )

        try {
            val inputStream = requireContext()           // open stream to read picked file
                .contentResolver
                .openInputStream(uri)

            val outputStream = java.io.FileOutputStream(output) // open stream to write our copy

            var read: Int
            val buffers = ByteArray(1024)                // read 1024 bytes at a time
            while (inputStream?.read(buffers)            // keep reading until no more bytes
                    .also { if (it != null) read = it } != -1) {
                outputStream.write(buffers, 0,           // write what we read into our copy
                    buffers.size)
            }
            inputStream?.close()                         // close input stream
            outputStream.close()                         // close output stream

        } catch (e: Exception) {
            Log.e(tag, "copyFileToInternalStorage: ${e.message}")
            return null                                  // if anything went wrong → return null
        }

        return output.path                               // return path to our copy
    }                                                    // e.g. /data/.../files/ota_file_/firmware.bin

    //    private val otaFilePicker = registerForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri ->
//
//        uri ?: return@registerForActivityResult
//
//        val input = requireContext()
//            .contentResolver
//            .openInputStream(uri)
//            ?: return@registerForActivityResult
//
//        val temp = java.io.File(                                    // create a File object in our app's private cache folder
//            requireContext().cacheDir,
//            "ota_temp.bin"                                   // we always name it ota_temp.bin
//        )                                                          // this is OUR COPY of the firmware file
//
//
//        temp.outputStream().use {
//            input.copyTo(it)
//        }
//        Log.i(tag, "OTA file: ${temp.absolutePath}" + "(${temp.length()} bytes")
//
//        viewModel.startOta(pendingOtaMac,temp.absolutePath)
//    }
}