package com.example.blescanner.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.aware.Characteristics
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


class BleScanService: Service(), BleScanCallBack {


    companion object {

        const val START_SCAN = "START_SCAN"
        const val STOP_SCAN = "STOP_SCAN"
        const val ACTION_SCAN_RESULT = "com.example.blescanner.SCAN_RESULT"
        const val ACTION_SCAN_TIMEOUT = "com.example.blescanner.SCAN_TIMEOUT"


//        const val START_LOCATE = "START_LOCATE"
//        const val STOP_LOCATE = "STOP_LOCATE"
//        const val ACTION_LOCATE_STARTED = "com.example.blescanner.LOCATE_STARTED"
//        const val ACTION_LOCATE_STOPPED = "com.example.blescanner.LOCATE_STOPPED"
//        const val ACTION_LOCATE_ERROR = "com.example.blescanner.LOCATE_ERROR"
//
//
//        const val START_PAIR = "START_PAIR"
//        const val STOP_PAIR = "STOP_PAIR"
//        const val ACTION_PAIRING_STARTED = "com.example.blescanner.PAIRING_STARTED"
//        const val ACTION_PAIRING_SUCCESS = "com.example.blescanner.PAIRING_SUCCESS"
//        const val ACTION_PAIRING_FAILED = "com.example.blescanner.PAIRING_FAILED"
//        const val EXTRA_PAIR_MAC = "pair_mac"
//        const val EXTRA_PAIR_ERROR = "pair_error"
//        const val EXTRA_PAIR_MESH_ID = "pair_mesh_id"


        const val START_CONNECT          = "START_CONNECT"
        const val STOP_CONNECT           = "STOP_CONNECT"
        const val ACTION_CONNECT_SUCCESS = "com.example.blescanner.CONNECT_SUCCESS"
        const val ACTION_CONNECT_FAILED  = "com.example.blescanner.CONNECT_FAILED"
        const val ACTION_CONNECT_STOPPED = "com.example.blescanner.CONNECT_STOPPED"
        const val ACTION_CONNECT_ERROR   = "com.example.blescanner.CONNECT_ERROR"
        const val ACTION_CONNECT_MAC      = "com.example.blescanner.CONNECT_MAC"

        const val START_OTA              = "START_OTA"
        const val ACTION_OTA_CLICKED     = "com.example.blescanner.OTA_CLICKED"
        const val ACTION_OTA_PROGRESS    = "com.example.blescanner.OTA_PROGRESS"
        const val ACTION_OTA_COMPLETE    = "com.example.blescanner.OTA_COMPLETE"
        const val ACTION_OTA_FAILED      = "com.example.blescanner.OTA_FAILED"

        const val EXTRA_OTA_NEW_FW_VERSION = "extra_ota_new_fw_version"
        const val EXTRA_OTA_STATE          = "extra_ota_state"

//
//        const val OTA_STATE_SCANNING_STARTED = 0
//        const val OTA_STATE_SCANNING     = 1
//        const val OTA_STATE_SCANNING_FINISHED =1
        const val OTA_STATE_FAILED = 2
        const val OTA_STATE_COMPLETED = 3
        const val OTA_STATE_IN_PROGRESS = 4
//        const val OTA_RETRY_COUNT = 3






        const val EXTRA_OTA_PROGRESS     = "extra_ota_progress"
        const val EXTRA_OTA_ERROR        = "extra_ota_error"
        const val EXTRA_OTA_FILE_PATH    = "extra_ota_file_path"
        const val EXTRA_OTA_DEVICE_UUID = "extra_ota_device_uuid"

//        const val EXTRA_HW_VERSION       = "extra_hw_version"
//        const val EXTRA_SW_VERSION       = "extra_sw_version"
//        const val EXTRA_FW_VERSION       = "extra_fw_version"

        const val EXTRA_MAC_ADDRESS = "extra_mac_address"
        const val EXTRA_CONNECT_ERROR = "extra_connect_error"
        const val EXTRA_BLE_DEVICE = "extra_ble_device"
        const val EXTRA_WITH_TIMER = "extra_with_timer"
//        const val SCAN_DURATION_MS = 60_000L

        private const val CHANNEL_ID = "ble_scan_channel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "BleScanService"

        const val DROP_T1_STATE_UNKNOWN    = 0
        const val DROP_T1_STATE_CONFIRMED  = 1
        const val DROP_T1_STATE_INITIATED  = 2
        const val DROP_T1_STATE_DOWNLOADED = 3
        const val DROP_T1_STATE_INSTALLED  = 4


        const val DROP_T1_CMD_INITIATE = 0X01
        const val DROP_T1_CMD_TRANSFER = 0X02
        const val DROP_T1_CMD_SWAP     = 0X03
        const val DROP_T1_CMD_CONFIRM  = 0X04

        const val EXTRA_DROP_T1_STATE   = "extra_drop_t1_state"
        const val EXTRA_DROP_T1_VERSION = "extra_drop_t1_version"



    }
//
//    val BLE_CHARACTERISTIC_DEVICE_ID = UUID.fromString("a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3");
//    val BLE_CHARACTERISTIC_NETWORK_ID = UUID.fromString("a2a2a2a2-a2a2-a2a2-a2a2-a2a2a2a2a2a2");
//    val BLE_CHARACTERISTIC_UUID = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
//    val BLE_CHARACTERISTIC_CONNECTABLE = UUID.fromString("a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5");
//    val BLE_CHARACTERISTIC_NETWORK_KEY = UUID.fromString("a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7");
//    val BLE_CHARACTERISTIC_CONNECTABLE_TIME = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
//    val BLE_SERVICE_TO_PAIR = UUID.fromString("a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0");
//    val BLE_SERVICE_TO_DEVICE_INFO = UUID.fromString("0000feb5-0000-1000-8000-00805f9b34fb");
//    val BLE_CHARACTERISTIC_S_W_INFO = UUID.fromString("d6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6");
//    val BLE_CHARACTERISTIC_F_W_INFO = UUID.fromString("d5d5d5d5-d5d5-d5d5-d5d5-d5d5d5d5d5d5");
//    val BLE_CHARACTERISTIC_H_W_INFO = UUID.fromString("d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4");
//
//    val BLE_SERVICE_FOR_SECURED_PAIRING = UUID.fromString("f0f0f0f0-f0f0-f0f0-f0f0-f0f0f0f0f0f0")
//    val BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
//    val BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA = UUID.fromString("f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2");
//
//    val BLE_SERVICE_FOR_TEST_CONTROL = UUID.fromString("b0b0b0b0-b0b0-b0b0-b0b0-b0b0b0b0b0b0");
//    val BLE_CHARACTERISTIC_FOR_TEST_CONTROL = UUID.fromString("b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1")
//
//
//    val OTA_BLE_SERVICE = UUID.fromString("e0e0e0e0-e0e0-e0e0-e0e0-e0e0e0e0e0e0")
////    val OTA_BLE_CHARACTERISTIC_UUID_DATA_WRITE = UUID.fromString("e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1")
////    val OTA_BLE_CHARACTERISTIC_UUID_DATA_WRITE = UUID.fromString("e3e3e3e3-e3e3-e3e3-e3e3-e3e3e3e3e3e3")
//    val OTA_BLE_CHARACTERISTIC_UUID_DATA_WRITE = UUID.fromString("e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5")
//
//
//    val OTA_BLE_CHARACTERISTIC_FLAG_UUID = UUID.fromString("e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2")
//
////    val BLE_CHARACTERISTIC_UUID_DATA_WRITE_WITH_HEADER = UUID.fromString("e3e3e3e3-e3e3-e3e3-e3e3-e3e3e3e3e3e3");


    val DROP_T1_SERVICE_UUID = UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB")

    val DROP_T1_CHAR_UUID    = UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB")





//    private val MODE_IDLE = -1
//    private val MODE_TEST = 0
//    private val MODE_TEST_DATA_WRITTEN = 1
//
//    private val MODE_WRITE_SECURITY_CODE = 2
//    private val MODE_WRITE_FIRST_PAIRING_DATA = 3
//    private val MODE_WRITE_SECOND_PAIRING_DATA = 4
//    private val MODE_WRITE_COMPLETED = 5
//    private val MODE_SOFTWARE_READ_COMPLETED = 6
//    private val MODE_DEVICE_PAIRING_COMMAND_WRITTEN = 144
//    private var sPairingMode = -1
//
//    private var locateGatt: BluetoothGatt? = null
//
//    private var gattToClose: BluetoothGatt? = null
//    private var writtenData: ByteArray? = null
//    private var mDeviceIdAssigned = 0
//    private var macAddress: String? = null

    private var connectGatt: BluetoothGatt? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var scanTimerJob: Job? = null
    private var isScanning = false
    private var blinkJob: Job? = null
    private var isLocating = false
    private var lastBlinkOperation: Int = 0x00
    private lateinit var scanningUtility: BleScanningUtility

    private var otaBytes: ByteArray? = null
    private var otaCurrentIndex = 0
    private var otaMacAddress = ""
    private var otaDeviceUuid = ""
    private var mMtuValueChunkSize = 16

    private var connectedHwVersion = ""
    private var connectedSwVersion = ""
    private var connectedFwVersion = ""

    private var otaStartTime = 0L
    private var otaConnectTime = 0L
    private var otaServiceDiscoveryTime = 0L
    private var otaMtuTime = 0L
    private var otaDataStartTime = 0L

    private var isOtaVersionRead = false

    private var dropT1OtaState   = DROP_T1_STATE_UNKNOWN
    private var isServiceDiscovered = false
    private var dropT1AllChunkSent = false
    private var dropT1LastCmd: Int = 0
    private val DROP_T1_REQUEST_MTU = 244



    override fun onCreate() {
        super.onCreate()
        scanningUtility = BleScanningUtility(this)
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        when (intent?.action) {
            START_SCAN -> {
                val withTimer = intent.getBooleanExtra(EXTRA_WITH_TIMER, true)
                startScan(withTimer)
            }

            STOP_SCAN -> stopScan()

//            START_LOCATE -> {
//                val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
//                if (macAddress != null) {
//                    Log.i(TAG, "Start locate: $macAddress")
//                    startLocate(macAddress)
//                } else {
//                    Log.e(TAG, "No MAC address provided")
//                }
//            }
//
//            STOP_LOCATE -> stopLocate()

            START_CONNECT -> {
                val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
                if (macAddress != null) {
                    Log.i(TAG, "Start connect: $macAddress")
                    startConnect(macAddress)
                } else {
                    Log.e(TAG, "No MAC address for connect")
                }
            }

            STOP_CONNECT -> {
                val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
                Log.i(TAG, "Stop connect: $macAddress")
                stopConnect(macAddress ?: "")
            }

            START_OTA -> {
                val mac = intent.getStringExtra(EXTRA_MAC_ADDRESS) ?: ""
                val filePath = intent.getStringExtra(EXTRA_OTA_FILE_PATH) ?: ""
                val uuid = intent.getStringExtra(EXTRA_OTA_DEVICE_UUID)  ?: ""
                Log.i(TAG, "OTA clicked: $mac file= $filePath")
//                startOta(mac, filePath,uuid)                                    //-----FOR WISILICA ----------------

                startDropT1Ota(mac,filePath)

                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ACTION_OTA_CLICKED).apply {
                        putExtra(EXTRA_MAC_ADDRESS, mac)
                    })
            }

        }

        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? = null



    override fun onScanResult(bleDevice: BleScannerScanResult) {
        Log.i(TAG, "Device found: ${bleDevice.device.address}")

        val intent = Intent(ACTION_SCAN_RESULT).apply {
            putExtra(EXTRA_BLE_DEVICE, bleDevice)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onError(errorCode: Int) {
        Log.e(TAG, "Scan error: $errorCode")
    }

    override fun onScanFinish() {
        Log.d(TAG, "Scan finished")
    }

    override fun onScanStart() {
        Log.d(TAG, "Scan started")
    }


    // ── Scan control ──────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startScan(withTimer: Boolean) {
        if (isScanning) {
            Log.d(TAG, "Already scanning — skip")
            return
        }
        isScanning = true

        scanningUtility.startBleScan(this)

    }

    private fun stopScan() {
        if (!isScanning) return
        isScanning = false
        scanTimerJob?.cancel()
        scanningUtility.stopBleScan()
        Log.d(TAG, "Scan stopped")
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                "BLE Scanner Service",
                NotificationManager.IMPORTANCE_LOW
            ).also {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(it)
            }
        }
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BLE Scanner")
            .setContentText("Scanning for BLE devices…")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()

    override fun onDestroy() {
        stopScan()
        stopConnect("")
//        stopLocate()
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }





    @SuppressLint("MissingPermission")
    private fun startConnect(mac: String) {
        if (connectGatt != null) {
            Log.w(TAG, "Already Connected- skip")
            return
        }
        Log.d(TAG, "startConnect: mac = $mac")
        val device = scanningUtility.bluetoothAdapter.getRemoteDevice(mac)
        BleConnectUtility.connectToDevice(this, device, connectGattCallBack)
    }


    @SuppressLint("MissingPermission")
    private fun stopConnect(mac: String) {
        connectGatt?.disconnect()
        connectGatt?.close()
        connectGatt = null
        Log.d(TAG, "stopConnect  called")
    }


    @SuppressLint("MissingPermission")
    private val connectGattCallBack: BluetoothGattCallback = object : BluetoothGattCallback() {

        // Step 1: Connection state changed
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    Log.d(TAG, "connectGattCallback: CONNECTED || CONNECTED || CONNECTED || CONNECTED -> discoverServices")
                    connectGatt = gatt
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestMtu(244)
//                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    Log.d(TAG, "connectGattCallback: DISCONNECTED || DISCONNECTED || DISCONNECTED || DISCONNECTED")
                    val mac = gatt.device.address
                    gatt.close()
                    connectGatt = null


                    if (dropT1OtaState == DROP_T1_STATE_DOWNLOADED) {
                        Log.i(TAG,"DropT1: disconnected after SWAP -> reconnecting in 1s")
                        serviceScope.launch {
                            delay(1000L)                                        //---waiting for 1 seconds as per the requirement
                            val device = scanningUtility.bluetoothAdapter.getRemoteDevice(mac)
                            BleConnectUtility.connectToDevice(
                                this@BleScanService,device,connectGattCallBack
                            )
                        }
                        return
                    }

                    otaBytes = null                                                         //----normal disconnect----------------
                    otaCurrentIndex = 0
                    LocalBroadcastManager.getInstance(this@BleScanService)
                        .sendBroadcast(Intent(ACTION_CONNECT_STOPPED).apply {
                            putExtra(EXTRA_MAC_ADDRESS, mac)
                        })
                }
            }
        }


        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

//            mMtuValueChunkSize = 243
//            val maxPayload = mtu - 3                                // mtu - 3 because BLE protocol uses 3 bytes for its own header  --WISILICA
//            val maxPayload = mtu - 1                                //  mtu - 1   DROP T1

//            mMtuValueChunkSize = (maxPayload / 16) * 16
//
//            if (mMtuValueChunkSize < 16) mMtuValueChunkSize = 16
            val attPayLoad = minOf(mtu,DROP_T1_REQUEST_MTU) - 3
            mMtuValueChunkSize = attPayLoad - 1


            Log.i(TAG, "OTA: MTU agreed = $mtu attPayLoad = $attPayLoad chunk size = $mMtuValueChunkSize")
            gatt?.discoverServices()

        }



        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "connectGattCallback: SERVICES DISCOVERED -> ready!")

//                gatt.services.forEach { service ->
//                    Log.d(TAG, "Service found: ${service.uuid}")
//                    service.characteristics.forEach { char ->
//                        Log.d(TAG, "Characteristic found: ${char.uuid}")
//                    }
//                }
//                if (isOtaVersionRead) {
//                    Log.i(TAG, "OTA: connected after reboot -> reading new FW version")
//                    val fwService = gatt.getService(BLE_SERVICE_TO_DEVICE_INFO)
//                    val hwChar = fwService?.getCharacteristic(BLE_CHARACTERISTIC_H_W_INFO)
//                    if (hwChar != null) {
//                        gatt.readCharacteristic(hwChar)  // start HW → SW → FW chain
//                    } else {
//                        Log.w(TAG, "OTA: HW char not found")
//                        broadcastOtaFailed("Version read failed", otaMacAddress)
//                    }
//                    return  // ← don't fall through to normal connect flow
//                }
//
//                //----Check if OTA is pending-------------------------------------------------------
//                if (otaBytes != null) {
//                    Log.i(TAG, "OTA: services discovered -> requesting MTU")
//                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
//                    gatt.requestMtu(251)
////                    writeNextOtaChunk()
//                    return
//                }
//
//
//                //------READING HW--SW--FW VERSION--------------------------------------------------
//                val hwService = gatt.getService(BLE_SERVICE_TO_DEVICE_INFO)
//                val hwChar = hwService?.getCharacteristic(BLE_CHARACTERISTIC_H_W_INFO)
//
//                if (hwChar != null) {
//                    Log.d(TAG, "Reading HW version...")
//                    gatt.readCharacteristic(hwChar)
//                } else {
//                    Log.w(TAG, "HW version char not found -> connecting without version")
//
//
//                    LocalBroadcastManager.getInstance(this@BleScanService)
//                        .sendBroadcast(Intent(ACTION_CONNECT_SUCCESS).apply {
//                            putExtra(EXTRA_MAC_ADDRESS, gatt.device.address)
//                            putExtra(EXTRA_HW_VERSION, connectedHwVersion)
//                            putExtra(EXTRA_SW_VERSION, connectedSwVersion)
//                            putExtra(EXTRA_FW_VERSION, connectedFwVersion)
//                        })
//                }

                val dropT1Service = gatt.getService(DROP_T1_SERVICE_UUID)
                val dropT1Char    =  dropT1Service?.getCharacteristic(DROP_T1_CHAR_UUID)

                if (dropT1Char != null) {
                    Log.d(TAG, "DropT1: reading characteristic 0xDDDD....................")
                    gatt.readCharacteristic(dropT1Char)
                } else {
                    Log.w(TAG, "DropT1: char not found -> connecting without version...")
                    broadcastConnectSuccess(gatt.device.address)
                }
            } else {

                Log.e(TAG, "connectGattCallback: SERVICES DISCOVERED FAILED status = $status")
                LocalBroadcastManager.getInstance(this@BleScanService)
                    .sendBroadcast(Intent(ACTION_CONNECT_FAILED).apply {
                        putExtra(EXTRA_MAC_ADDRESS, gatt.device.address)
                        putExtra(EXTRA_CONNECT_ERROR, "Service discovery failed")
                    })
                gatt.disconnect()
            }
        }




        // Step 3: These will be used for OTA later

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onCharacteristicRead failed: ${ characteristic.uuid}")
                return
            }

            val value = characteristic.value ?: return

            when (characteristic.uuid) {

//                BLE_CHARACTERISTIC_H_W_INFO -> {
//
//                    connectedHwVersion = getVersionFromBytes(value)                  //---HW version read ->  store  -> read SW version next
//                    Log.d(TAG, "HW version: $connectedHwVersion")
//
//                    val swChar = gatt.getService(BLE_SERVICE_TO_DEVICE_INFO) ?.getCharacteristic(BLE_CHARACTERISTIC_S_W_INFO)
//
//                    if(swChar != null) {
//                        gatt.readCharacteristic(swChar)
//                    } else {
//                        Log.w(TAG, "SW version char not found ")
//                        broadcastConnectSuccess(gatt.device.address)
//                    }
//                }
//
//                BLE_CHARACTERISTIC_S_W_INFO -> {
//                    connectedSwVersion = getVersionFromBytes(value)
//                    Log.d(TAG, "SW version: $connectedSwVersion")
//
//                    val fwChar = gatt.getService(BLE_SERVICE_TO_DEVICE_INFO) ?.getCharacteristic(BLE_CHARACTERISTIC_F_W_INFO)
//
//                    if(fwChar != null) {
//                        gatt.readCharacteristic(fwChar)
//                    } else {
//                        Log.w(TAG, "FW version char not found ")
//                        broadcastConnectSuccess(gatt.device.address)
//                    }
//                }
//
//                BLE_CHARACTERISTIC_F_W_INFO -> {
//
//                    val newFwVersion = getVersionFromBytes(value)
//
//                    Log.d(TAG, "FW version: $newFwVersion")
//
//                    if (isOtaVersionRead) {
//
//                        isOtaVersionRead = false
//                        Log.i(TAG, "==========================================")
//                        Log.i(TAG, "OTA COMPLETE SUMMARY:")
//                        Log.i(TAG, "New FW version = $newFwVersion")
//                        Log.i(TAG, "==========================================")
//
//
//                        LocalBroadcastManager.getInstance(this@BleScanService)
//                            .sendBroadcast(Intent(ACTION_OTA_COMPLETE).apply {
//                                putExtra(EXTRA_MAC_ADDRESS, otaMacAddress)
//                                putExtra(EXTRA_OTA_NEW_FW_VERSION, newFwVersion)
//                                putExtra(EXTRA_OTA_STATE, OTA_STATE_COMPLETED)
//                            })
//                        otaMacAddress = ""
//                        otaDeviceUuid = ""
//                    } else {
//                        connectedFwVersion = newFwVersion
//                        broadcastConnectSuccess(gatt.device.address)
//                    }
//                }

                DROP_T1_CHAR_UUID -> {

                    val (state,returnCode,version) = DropT1StateResponse(value)      //---calling the device in which state (as 0, 1, 2,...)-----

                    if (returnCode != 0) {

                        Log.e(TAG,"DROP T1: return code = $returnCode -> error")
                        broadcastOtaFailed("Device returned error code: $returnCode",otaMacAddress)
                        return
                    }

                    dropT1OtaState = state
                    Log.i(TAG,"DropT1: state = $state version = $version")


                    when (state) {

                        DROP_T1_STATE_CONFIRMED -> {

                            Log.i(TAG,"DropT1: CONFIRMED -> BROADCASTING CONNECT SUCCESS")

                            LocalBroadcastManager.getInstance(this@BleScanService)
                                .sendBroadcast(Intent(ACTION_CONNECT_SUCCESS).apply {
                                    putExtra(EXTRA_MAC_ADDRESS, gatt.device.address)
                                    putExtra(EXTRA_DROP_T1_STATE, state)
                                    putExtra(EXTRA_DROP_T1_VERSION, version)
                                })
                        }


                        DROP_T1_STATE_INITIATED -> {

                            if (dropT1AllChunkSent) {

                                Log.w(
                                    TAG,
                                    "DropT1: all chunks sent but still INITIATED -> waiting 3s more"
                                )
                                serviceScope.launch {
                                    delay(3000L)
                                    val g = connectGatt ?: return@launch
                                    readDropT1State(g)
                                }
                            } else {
                                Log.i(TAG, "DropT1: INITIATED || INITIATED || INITIATED || INITIATED || INITIATED || INITIATED  -> sending TRANSFER")
                                sendNextDropT1Chunk()
                            }
                        }

                        DROP_T1_STATE_DOWNLOADED -> {

                            Log.i(TAG,"DropT1: DOWNLOADED || DOWNLOADED || DOWNLOADED || DOWNLOADED || DOWNLOADED  ->  sending SWAP")
                            sendDropT1Swap()
                        }

                        DROP_T1_STATE_INSTALLED -> {

                            Log.i(TAG,"DropT1: INSTALLED || INSTALLED || INSTALLED || INSTALLED || INSTALLED  ->  sending CONFIRM")
                            sendDropT1Confirm()
                        }

                        else -> {
                            Log.w(TAG,"DropT1: unknown state = $state")
                        }
                    }



                }

                else -> {
                    Log.d(TAG, "onCharacteristicRead: ${characteristic.uuid}")
                }
            }


            // OTA: read FW version here later
//            Log.d(TAG, "onCharacteristicsRead: ${characteristic.uuid}")
        }


        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            // OTA: write firmware chunks here later
            if (status != BluetoothGatt.GATT_SUCCESS) {

                if (dropT1LastCmd == DROP_T1_CMD_SWAP) {
                    Log.i(TAG,"DropT1: SWAP write status = $status -> expected disconnect, waiting for reconnect")
                    return
                }

                Log.d(TAG, "onCharacteristicWrite: ${characteristic?.uuid} status = $status")
                broadcastOtaFailed("Write failed status= $status", otaMacAddress)
                return
            }

            when (characteristic?.uuid) {

//                OTA_BLE_CHARACTERISTIC_UUID_DATA_WRITE -> {
//
//                    writeNextOtaChunk()
//                }
//
//                OTA_BLE_CHARACTERISTIC_FLAG_UUID -> {
//
//                    Log.i(TAG, "OTA: flag written -> OTA COMPLETE || OTA COMPLETE || OTA COMPLETE || OTA COMPLETE ........DEVICE REBOOTING......WAITING ")
//                    otaBytes = null
//                    otaCurrentIndex = 0
//                    isOtaVersionRead = true
//
//                    serviceScope.launch {
//                        delay(20_000L)                          //-------delay 20seconds------------// ↑ Device needs this time to:
//                        //   1. Save new firmware to flash memory
//                        //   2. Reboot
//                        //   3. Start advertising again
//
//                        Log.i(TAG, "OTA:waiting is done -> starting version scan")
//                        startOtaVersionScan(otaMacAddress,otaDeviceUuid)
//
//                    }
//                }


//                DROP_T1_CHAR_UUID -> {
//
//                    Log.i(TAG,"DropT1: write complete ")
//
//                    if (dropT1AllChunkSent) {
//                        Log.i(TAG,"DropT1: all chunks write confirmed -> reading state")
//                        serviceScope.launch {
//                            delay(1000L)                                            //-----wait 1s for device to verify length + checksum------------------
//                            val g = connectGatt ?: return@launch
//                            readDropT1State(g)
//                        }
//                    } else if (otaBytes != null) {
//                        Log.i(TAG,"DropT1: chunk write confirmed -> sending next chunk")
//                        sendNextDropT1Chunk()                                                 //---- chunk write confirmed → send next chunk
//
//                    }
////                    val g = connectGatt ?: return
////                    readDropT1State(g)                                          // ← read char → triggers onCharacteristicRead
//                }

                DROP_T1_CHAR_UUID -> {

                    Log.i(TAG, "DropT1: write complete cmd=0x${"%02X".format(dropT1LastCmd)}")

                    when (dropT1LastCmd) {

                        DROP_T1_CMD_INITIATE -> {
                            // ← INITIATE confirmed → read state → expect state=2
                            Log.i(TAG, "DropT1: INITIATE confirmed → reading state")
                            serviceScope.launch {
                                delay(300L)
                                val g = connectGatt ?: return@launch
                                readDropT1State(g)
                            }
                        }

                        DROP_T1_CMD_TRANSFER -> {
                            // ← chunk confirmed → send next OR read state if all done
                            if (dropT1AllChunkSent) {
                                Log.i(TAG, "DropT1: last chunk confirmed → reading state")
                                serviceScope.launch {
                                    delay(1000L)
                                    val g = connectGatt ?: return@launch
                                    readDropT1State(g)
                                }
                            } else {
                                Log.i(TAG, "DropT1: chunk confirmed → next chunk")
                                sendNextDropT1Chunk()
                            }
                        }

                        DROP_T1_CMD_SWAP -> {
                            // ← device will reboot → onConnectionStateChange handles reconnect
                            Log.i(TAG, "DropT1: SWAP confirmed → device rebooting")
                        }

                        DROP_T1_CMD_CONFIRM -> {
                            // ← CONFIRM confirmed → read state → expect state=1
                            Log.i(TAG, "DropT1: CONFIRM confirmed → reading state")
                            serviceScope.launch {
                                delay(300L)
                                val g = connectGatt ?: return@launch
                                readDropT1State(g)
                            }
                        }
                    }
                }


                else -> {
                    Log.d(TAG, "onCharacteristicWrite: ${characteristic?.uuid} status = $status")
                }
            }
        }

    }


    private fun DropT1StateResponse (value : ByteArray) : Triple<Int , Int , String> {

        Log.i(TAG, "DropT1 raw bytes: ${value.take(9).map { "0x%02X".format(it) }}")


        val state      = value[0].toInt() and 0xFF                  //---byte 0 -> device state--------
        val returnCode = ((value[1].toInt() and 0xFF) shl 24) or  // ← Big Endian
                        ((value[2].toInt() and 0xFF) shl 16) or  // byte1=HIGH
                        ((value[3].toInt() and 0xFF) shl 8)  or
                        (value[4].toInt() and 0xFF)              // byte4=LOW

        val major      = value[5].toInt() and 0xFF                  //---byte 5 -> major version--------
        val minor      = value[6].toInt() and 0xFF                  //---byte 6 -> minor version--------
        val patch      = value[7].toInt() and 0xFF                  //---byte 7 -> patch version--------
        val build      = value[8].toInt() and 0xFF                  //---byte 8 -> build version--------
        val version    = "$major.$minor.$patch.$build"              // combine

        Log.i(TAG,"DropT1 parsed: state = $state returnCode = $returnCode version = $version")
        return Triple(state, returnCode, version)
    }



    //--------START OTA-------------------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startDropT1Ota(mac:String, filePath:String) {

        val  file = java.io.File(filePath)
        if (!file.exists()) {
            broadcastOtaFailed("File not found: $filePath", mac)
            return
        }

        val bytes = file.readBytes()                                    //---read all firmware bytes------------
        if (bytes.isEmpty()) {
            broadcastOtaFailed("File is empty",mac)
            return
        }
        otaBytes = bytes
        otaCurrentIndex = 0                                             //---start from byte 0
        otaMacAddress = mac
        dropT1AllChunkSent = false

        Log.i(TAG,"DropT1 OTA: file loaded ${bytes.size} bytes -> sending INITIATE")

        val gatt = connectGatt ?: run {
            broadcastOtaFailed("Not connected",mac)
            return
        }

//        val packet = buildInitiatePacket(bytes)
//        val success = writeDropT1Char(gatt,packet)          //---send INITIATE to device-------
//
//
//        if (success) {
//            serviceScope.launch {
//                delay(300L)
//                readDropT1State(gatt)                             //---read state -> expect state 2
//            }
//        } else {
//            broadcastOtaFailed("INITIATE write failed",mac)
//        }
////        writeDropT1Char(gatt,packet)
        val packet  = buildInitiatePacket(bytes)
        val success = writeDropT1Char(gatt, packet)

        if (!success) {
            broadcastOtaFailed("INITIATE write failed", mac)
        }
    }



    private fun buildInitiatePacket(fwBytes: ByteArray): ByteArray {

        val length = fwBytes.size                                   //-----TOTAL FIRMWARE FILE SIZE------------------------
        Log.i(TAG,"DropT1 OTA: file size || file size || file size = $length")

        var checksum = 0
        for (b in fwBytes) {
            checksum +=  (b.toInt() and 0xFF)
        }

        val packet = ByteArray(6)
        packet[0]  = DROP_T1_CMD_INITIATE.toByte()                  //---byte 0 = 0x01 (INITIATE command)
        packet[1]  = ((length shr 24) and 0xFF).toByte()
        packet[2]  = ((length shr 16) and 0xFF).toByte()
        packet[3]  = ((length shr 8) and 0xFF).toByte()
        packet[4]  = (length and 0xFF).toByte()
        packet[5]  =  (checksum and 0xFF).toByte()

        Log.i(TAG,"DropT1 INITIATE: length=$length checksumFull=$checksum checksumByte=0x${"%02X".format(checksum and 0xFF)}")
        return packet
    }



    @SuppressLint("MissingPermission")
    private fun writeDropT1Char(gatt: BluetoothGatt, data: ByteArray):Boolean {

        val service = gatt.getService(DROP_T1_SERVICE_UUID) ?:run {
            Log.e(TAG,"DropT1: service 0xDDDD not found")
            return false
        }

        val char = service.getCharacteristic(DROP_T1_CHAR_UUID) ?:run {
            Log.e(TAG,"DropT1: char 0xDDDD not found")
            return false
        }

        dropT1LastCmd  = data[0].toInt() and 0xFF          // ← store cmd byte BEFORE write
        char.value = data                                                           //---set bytes to write---------------
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT                 ///----WITH RESPONSE--------------------------------
//        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE             //  WITHOUT response —------------------------------

        val result = gatt.writeCharacteristic(char)

        Log.i(TAG,"DropT1: writeChar result = $result bytes = ${data.size}")
        return result
    }


    @SuppressLint("MissingPermission")
    private fun readDropT1State(gatt: BluetoothGatt) {

        val service = gatt.getService(DROP_T1_SERVICE_UUID) ?:run {
            Log.e(TAG,"DropT1: service 0xDDDD not found for read")
            return
        }

        val char = service.getCharacteristic(DROP_T1_CHAR_UUID) ?:run {
            Log.e(TAG,"DropT1: char 0xDDDD not found for read")
            return
        }

        gatt.readCharacteristic(char)                           //---ask device to send state bytes--------------
        Log.d(TAG,"DropT1: readChar triggered")                 //--triggers onCharacteristicRead
    }


    @SuppressLint("MissingPermission")
    private fun sendNextDropT1Chunk() {

        val all = otaBytes ?:return                                           //---firmware bytes stored in startDropT1Ota()
        val gatt = connectGatt ?: run {
            broadcastOtaFailed("GATT lost", otaMacAddress)
            return
        }

        if (otaCurrentIndex >= all.size) {
            dropT1AllChunkSent = true                                           //---- mark all chunks done--------------------
            Log.i(TAG, "DropT1: all chunks sent → waiting for onCharacteristicWrite to confirm")
            serviceScope.launch {
                delay(1000L)
                val g = connectGatt ?: return@launch
                readDropT1State(g)
            }
//            readDropT1State(gatt)
            return
        }

        //---------Slice next Chunk---------------------------------------------------
        val end = minOf(otaCurrentIndex + mMtuValueChunkSize,all.size)
        val chunk = all.copyOfRange(otaCurrentIndex,end)
        otaCurrentIndex = end


        val packet = ByteArray(1+ chunk.size)
        packet[0]  = DROP_T1_CMD_TRANSFER.toByte()
        chunk.copyInto(packet,1)


        val progress = otaCurrentIndex.toFloat() / all.size.toFloat() * 100f
        Log.d(TAG, "DropT1 CHUNK: $otaCurrentIndex/${all.size} (${"%.1f".format(progress)}%)")
        broadcastOtaProgress(progress,otaMacAddress)

        writeDropT1Char(gatt,packet)                // ← write → onCharacteristicWrite fires → next chunk

//        val success = writeDropT1Char(gatt,packet)
//
//        if (success) {
//            serviceScope.launch {
//                delay(20L)
//                sendNextDropT1Chunk()                    // ← call next chunk immediately after write queued
//            }
//        } else {
//            Log.w(TAG, "DropT1: write result=false → retrying in 50ms")
//            otaCurrentIndex -= chunk.size    // ← roll back index
//            serviceScope.launch {
//                delay(50L)
//                sendNextDropT1Chunk()        // ← retry same chunk
//            }
//
//        }
    }


    @SuppressLint("MissingPermission")
    private fun sendDropT1Swap() {
        val gatt = connectGatt ?:return
        val packet = byteArrayOf(DROP_T1_CMD_SWAP.toByte())                         //--- [0x03] --------------
        Log.i(TAG,"DropT1: sending SWAP [0X03]")
        writeDropT1Char(gatt,packet)                         // ← device installs firmware + reboots
    }

    @SuppressLint("MissingPermission")
    private fun sendDropT1Confirm() {
        val gatt = connectGatt ?:return
        val packet = byteArrayOf(DROP_T1_CMD_CONFIRM.toByte())                  //-----[0x04]-----------------
        Log.i(TAG,"DropT1: sending CONFIRM [0X04]")
        writeDropT1Char(gatt, packet)

//        val success = writeDropT1Char(gatt,packet)
//
//        if (success) {
//            serviceScope.launch {
//                delay(300L)
//                val g = connectGatt ?: return@launch
//                readDropT1State(g)
//            }
//        }
//
////        writeDropT1Char(gatt,packet)                        // ← device locks in new firmware permanently
    }














//
//    //--------START OTA-----------------------------------------------------------------------------
//
//    @SuppressLint("MissingPermission")
//    private fun startOta(mac: String, filePath: String , uuid: String) {
//
//        val file = java.io.File(filePath)
//        if (!file.exists()) {
//            broadcastOtaFailed("File not found: $filePath", mac)
//            return
//        }
//        val bytes = file.readBytes()
//        if (bytes.isEmpty()) {
//            broadcastOtaFailed("File is empty", mac)
//            return
//        }
//
//        // 2. Store OTA State
//        otaBytes = bytes
//        otaCurrentIndex = 0
//        otaMacAddress = mac
//        otaDeviceUuid = uuid
//        Log.i(TAG,"OTA: file loaded ${bytes.size} bytes")
//
//
//        // 3. Check if already connected
//        if (connectGatt != null) {
//            Log.i(TAG, "OTA: already connected, starting chunks")
//            connectGatt!!.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
//            connectGatt!!.requestMtu(251)
////            writeNextOtaChunk()
//        } else {
//
//            Log.i(TAG, "OTA: not connected, connecting first")
//            val device = scanningUtility.bluetoothAdapter.getRemoteDevice(mac)
//            BleConnectUtility.connectToDevice(this, device, connectGattCallBack)
//        }
//    }


//    @SuppressLint("MissingPermission")
//    private fun writeNextOtaChunk() {
//
//        val all = otaBytes?: return
//        val gatt = connectGatt?: run {
//            broadcastOtaFailed("GATT lost during OTA", otaMacAddress)
//            return
//        }
//
//        if (otaCurrentIndex >= all.size) {
//
//            Log.i(TAG,"OTA: all chunks sent -> writing flag")
//            writeOtaFlag(gatt)
//            return
//        }
//
//        val end = minOf(otaCurrentIndex + mMtuValueChunkSize, all.size)
//        val slice = all.copyOfRange(otaCurrentIndex, end)
//        val actualByteCount = slice.size
//        val paddedSize = ((slice.size + 3) / 4) * 4
//        val chunk = slice.copyOf(paddedSize)
//
//        otaCurrentIndex = end
//
//        //Calculate and broadcast(show) the progress
//        val progress = otaCurrentIndex.toFloat() / all.size.toFloat() * 100f
//        Log.d(TAG," OTA CHUNK: $otaCurrentIndex / ${all.size} (${"%.1f".format(progress)}%)")
//        broadcastOtaProgress(progress, otaMacAddress)
//
//
//        val service = gatt.getService(OTA_BLE_SERVICE) ?: run {
//            broadcastOtaFailed("OTA service e0e0e0e0e0e not found", otaMacAddress)
//            return
//        }
//        val char = service.getCharacteristic(OTA_BLE_CHARACTERISTIC_UUID_DATA_WRITE) ?: run {
//            broadcastOtaFailed("OTA data char  e1e1e1e1e not found", otaMacAddress)
//            return
//        }
//        char.value = getEncrytedPacketWithoutPaddingWithMtu(chunk,actualByteCount)                      //---AES
//        gatt.writeCharacteristic(char)
//    }
//
//
//
//    //----Write completion flag [0x01] to e2e2e2e2
//
//    @SuppressLint("MissingPermission")
//    private fun writeOtaFlag(gatt: BluetoothGatt) {
//
//        val service = gatt.getService(OTA_BLE_SERVICE) ?: run {
//            broadcastOtaFailed("OTA service not found for flag", otaMacAddress)
//            return
//        }
//
//        val char = service.getCharacteristic(OTA_BLE_CHARACTERISTIC_FLAG_UUID) ?: run {
//            broadcastOtaFailed("OTA flag char not found", otaMacAddress)
//            return
//        }
//        char.value = byteArrayOf(0x01)
//        gatt.writeCharacteristic(char)
//        Log.i(TAG,"OTA: flag [0x01] sent to e2e2e2e2e2e2")
//
//    }
//
//
//
//
//
//    @SuppressLint("MissingPermission")
//    private fun startOtaVersionScan(mac:String, uuid: String) {
//
//        Log.i(TAG, "OTA version scan: started looking for uuid=$uuid")
//
//        scanningUtility.startBleScan(object : BleScanCallBack {
//
//            override fun onScanResult(bleDevice: BleScannerScanResult) {
//
//                val scanData = bleDevice.scanData
//                if (scanData.size < 25) return
//
//                val manufacturerId =
//                    ((scanData[6].toInt() and 0xFF) shl 8) or (scanData[5].toInt() and 0xFF)
//
//                if (manufacturerId != 0x0197 && manufacturerId != 0x9701) return
//
//                val packetFormat = scanData[7].toInt() and 0xFF
//                if (packetFormat != 0x00 && packetFormat != 0x01) return
//
//                val scannedUuid = BleUtilis.getUUID(scanData)
//                Log.d(
//                    TAG, "OTA version scan: " +
//                            "scannedUuid=$scannedUuid " +
//                            "targetUuid=$uuid " +
//                            "mac=${bleDevice.device.address}"
//                )
//
//                if (scannedUuid.equals(uuid, ignoreCase = true)) {
//
//                    Log.i(TAG, "OTA: UUID matched!!!!!! device found ->stopping scan -> connecting")
//                    otaDeviceUuid = ""
//                    scanningUtility.stopBleScan()
//
//                    val device =
//                        scanningUtility.bluetoothAdapter.getRemoteDevice(bleDevice.device.address)
//                    BleConnectUtility.connectToDevice(
//                        this@BleScanService,
//                        device,
//                        connectGattCallBack
//                    )
//
//
//                }
//            }
//
//                override fun onError(errorCode: Int) {
//                    Log.e(TAG, "OTA version scan error: $errorCode")
//                    broadcastOtaFailed("Version scan failed", mac)
//                }
//
//                override fun onScanFinish() {
//                    if (otaDeviceUuid.isNotEmpty()) {
//                        Log.i(TAG, "OTA version scan: device not found yet -> restarting in 2s")
//                        serviceScope.launch {
//                            delay(2_000L)
//                            startOtaVersionScan(mac, uuid)
//                        }
//                    }
//                }
//
//                override fun onScanStart() {
//                    Log.d(TAG, "OTA version scan started")
//                }
//
//        })
//    }








    private fun broadcastOtaProgress (progress: Float, mac: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_OTA_PROGRESS).apply {
            putExtra(EXTRA_OTA_PROGRESS, progress)
            putExtra(EXTRA_MAC_ADDRESS, mac)
        })
    }


    private fun broadcastOtaFailed(reason: String, mac: String) {
        Log.e(TAG,"OTA FAILED: $reason")
        otaBytes = null
        otaCurrentIndex = 0
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_OTA_FAILED).apply {
                putExtra(EXTRA_OTA_ERROR, reason)
                putExtra(EXTRA_MAC_ADDRESS, mac)
            })
    }


    private fun broadcastConnectSuccess(mac: String) {
        Log.i(TAG, "Connect success: $mac" + "HW= $connectedHwVersion SW= $connectedSwVersion FW= $connectedFwVersion")

        LocalBroadcastManager.getInstance(this@BleScanService)
            .sendBroadcast(Intent(ACTION_CONNECT_SUCCESS).apply {
                putExtra(EXTRA_MAC_ADDRESS, mac)
//                putExtra(EXTRA_HW_VERSION, connectedHwVersion)
//                putExtra(EXTRA_SW_VERSION, connectedSwVersion)
//                putExtra(EXTRA_FW_VERSION, connectedFwVersion)
            })
        connectedHwVersion = ""
        connectedSwVersion = ""
        connectedFwVersion = ""
    }


//    private fun getVersionFromBytes(bytes: ByteArray): String {
//        if (bytes.size < 3) return "unknown"
//                                                                                        // Lumos uses hex (ByteUtility.getVersionFromByteArray)
//        return Integer.toHexString(bytes[0].toInt() and 0xFF) +
//                "." + Integer.toHexString(bytes[1].toInt() and 0xFF) +
//                "." + Integer.toHexString(bytes[2].toInt() and 0xFF)
//    }

    // This is the AES key for encrypting OTA data
// Decoded from Lumos SharedPreferences "network_key" = "2hTBEl3OBxQWpBGaqzrl/g=="
    private val NETWORK_KEY = byteArrayOf(
        0xDA.toByte(), 0x14, 0xC1.toByte(), 0x12,
        0x5D, 0xCE.toByte(), 0x07, 0x14,
        0x16, 0xA4.toByte(), 0x11, 0x9A.toByte(),
        0xAB.toByte(), 0x3A, 0xE5.toByte(), 0xFE.toByte()
    )

//
//    private fun getEncrytedPacketWithoutPaddingWithMtu(data: ByteArray, mtuValue: Int): ByteArray {
//
//        val encryptedPacket = ByteArray(data.size + 1)
//        encryptedPacket[0] = mtuValue.toByte()
//
//        val key = CharArray(NETWORK_KEY.size) { (NETWORK_KEY[it].toInt() and 0xFF).toChar() }
//
//        val rc5 = RC5()
//        rc5.rc5KeyIninitialize(key)
//
//        var startIndex = 0
//        while (startIndex + 4 <= mtuValue) {
//            val block = data.copyOfRange(startIndex, startIndex + 4)
//            val encryptedBlock = rc5.rc5Encrypt(block)
//            for (i in startIndex until startIndex + 4) {
//                encryptedPacket[i + 1] = encryptedBlock[i - startIndex]
//            }
//            startIndex += 4
//        }
//
//        return encryptedPacket
//    }
//    private fun getEncrytedPacketWithoutPaddingWithMtu(data: ByteArray): ByteArray {
//
//        val result = mutableListOf<Byte>()
//
//        var pos = 0                         // start from byte 0 of the chunk
//
//        while (pos < data.size) {
//
//            val block = data.copyOfRange(pos, minOf(pos + 16, data.size))
//
//            // If last block has less than 16 bytes, pad with zeros
//            // AES/ECB/NoPadding requires EXACTLY 16 bytes — no exceptions
//            val padded = if (block.size < 16) block.copyOf(16) else block
//
//            val encrypted = aesEncrypt(padded)
//
//            result.addAll(encrypted.toList())
//            pos += 16
//
//        }
//        return result.toByteArray()
//    }
//
//    private fun aesEncrypt (data: ByteArray): ByteArray {
//
//        val secretKey = javax.crypto.spec.SecretKeySpec(NETWORK_KEY, "AES")
//        val cipher = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding")
//        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
//        return cipher.doFinal(data)
//    }



























//    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
//    @SuppressLint
//    private fun startLocate(macAddress: String) {
//        stopLocate()
//        sPairingMode = MODE_IDLE
//
//        val device = scanningUtility.bluetoothAdapter.getRemoteDevice(macAddress)
//
//        BleConnectUtility.connectToDevice(
//            this,
//            device,
//            object : BluetoothGattCallback() {
//
//                @SuppressLint("MissingPermission")
//                override fun onConnectionStateChange(
//                    gatt: BluetoothGatt,
//                    status: Int,
//                    newState: Int
//                ) {
//                    when (newState) {
//                        BluetoothProfile.STATE_CONNECTED -> {
//                            BleConnectUtility.cancelConnectionTimeout()
//                            Log.i(TAG, "onConnectionStateChange/connected: ${gatt.device.address}")
//                            locateGatt = gatt
//                            gatt.discoverServices()
//                        }
//
//                        BluetoothProfile.STATE_DISCONNECTED -> {
//                            BleConnectUtility.cancelConnectionTimeout()
//                            Log.i(
//                                TAG,
//                                "onConnectionStateChange/disconnected: ${gatt.device.address}"
//                            )
//                            gatt.close()
//                            locateGatt = null
//                            sPairingMode = MODE_IDLE
//                            LocalBroadcastManager.getInstance(this@BleScanService)
//                                .sendBroadcast(Intent(ACTION_LOCATE_STOPPED))
//                        }
//                    }
//                }
//
//                @SuppressLint("MissingPermission")
//                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//
//                        val securedService = gatt.getService(BLE_SERVICE_FOR_SECURED_PAIRING)
//                        if (securedService != null) {
//                            sPairingMode = MODE_TEST
//                            writeValueToCharacteristic(
//                                gatt,
//                                BLE_SERVICE_FOR_SECURED_PAIRING,
//                                BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//                                byteArrayOf(0x01)
//                            )
//                        } else {
//                            val success = writeValueToCharacteristic(
//                                gatt,
//                                BLE_SERVICE_FOR_TEST_CONTROL,
//                                BLE_CHARACTERISTIC_FOR_TEST_CONTROL,
//                                byteArrayOf(0x01)
//                            )
//                            if (success) {
//                                LocalBroadcastManager.getInstance(this@BleScanService)
//                                    .sendBroadcast(Intent(ACTION_LOCATE_STARTED))      //-------BLINKIND-----------------------
//                            } else {
//                                gatt.disconnect()
//                                LocalBroadcastManager.getInstance(this@BleScanService)
//                                    .sendBroadcast(Intent(ACTION_LOCATE_ERROR))
//                            }
//                        }
//                    } else {
//                        Log.e(TAG, "onServiceDiscovered failed: $status")
//                        gatt.disconnect()
//                        LocalBroadcastManager.getInstance(this@BleScanService)
//                            .sendBroadcast(Intent(ACTION_LOCATE_ERROR))
//                    }
//                }
//
//
//                // Step 3 — called after every write completes
//                // sPairingMode tracks which step just finished
//                @SuppressLint("MissingPermission")
//                override fun onCharacteristicWrite(
//                    gatt: BluetoothGatt,
//                    characteristic: BluetoothGattCharacteristic,
//                    status: Int
//                ) {
//                    if (status != BluetoothGatt.GATT_SUCCESS) {
//                        Log.e(TAG, "onCharacteristicsWrite field: ${characteristic.uuid}")
//                        return
//                    }
//
//                    when (sPairingMode) {
//
//                        MODE_TEST -> {
//
//                            Log.i(
//                                TAG,
//                                "MODE_TEST -> writing test packet to f2f2f2f2f2f2f2f2f2f2f2f2f2f2f2"
//                            )
//                            lastBlinkOperation = 0x01
//                            val testData =
//                                generateTestPacketWithoutEncryption(0x01)  // ← 0x01 = ON/blink
//
//                            sPairingMode = MODE_TEST_DATA_WRITTEN
//                            writeValueToCharacteristic(
//                                gatt,
//                                BLE_SERVICE_FOR_SECURED_PAIRING,
//                                BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA,
//                                testData
//                            )
//                        }
//
//                        MODE_TEST_DATA_WRITTEN -> {
//
//                            Log.i(
//                                TAG,
//                                "MODE_TEST_DATA_WRITTEN -> device blinking --------------f2f2f2f2f2f2f2f2f2f2f2f2f2f2f2"
//                            )
//
//                            sPairingMode = MODE_IDLE
//
//                            if (!isLocating) {
//                                isLocating = true
//
//                                LocalBroadcastManager.getInstance(this@BleScanService)
//                                    .sendBroadcast(Intent(ACTION_LOCATE_STARTED))
//                                Log.i(TAG, "Locate Started")
//                            }
//
//                            blinkJob?.cancel()
//                            blinkJob = serviceScope.launch {
//                                delay(500L)
//
//                                if (!isLocating) return@launch
//
//                                val nextOperation = if (lastBlinkOperation == 0x01) 0x00 else 0x01
//                                lastBlinkOperation = nextOperation
//
//                                val testData = generateTestPacketWithoutEncryption(nextOperation)
//
//                                sPairingMode = MODE_TEST_DATA_WRITTEN
//                                writeValueToCharacteristic(
//                                    gatt,
//                                    BLE_SERVICE_FOR_SECURED_PAIRING,
//                                    BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA,
//                                    testData
//                                )
//                                Log.d(TAG, "Blink → ${if (nextOperation == 0x01) "ON" else "OFF"}")
//                            }
//
//                        }
//
////                            LocalBroadcastManager.getInstance(this@BleScanService)
////                                .sendBroadcast(Intent(ACTION_LOCATE_STARTED))
//
//                    }
//                }
//            }
//        )
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun stopLocate() {
//        isLocating = false
//        blinkJob?.cancel()
//        blinkJob = null
//        locateGatt?.disconnect()
//        locateGatt?.close()
//        locateGatt = null
//        sPairingMode = MODE_IDLE
//        Log.d(TAG, "Stop locate")
//    }
//
//
//    private fun generateTestPacketWithoutEncryption(mTestOperation: Int): ByteArray {
//
//        val testData = ByteArray(16)
//        testData[0] = mTestOperation.toByte()
//        return testData
//    }




//
//    //---------PAIRING FUNCTION------------------------------------------------------------------
//
//
//    @SuppressLint("MissingPermission")
//    private fun startPairing(mac: String) {
//
//        if (gattToClose != null) {
//            Log.w(TAG, "Pairing already in progress- skip")
//            return
//        }
//
//        macAddress = mac
//
//        mDeviceIdAssigned = BlePairingPacketHandler.createRandomMeshId()
//
//        BlePairingPacketHandler.init()
//
//        sPairingMode = MODE_IDLE
//
//        Log.d(TAG, "startPairing: mac = $mac meshId = $mDeviceIdAssigned")
//
//        LocalBroadcastManager.getInstance(this)
//            .sendBroadcast(Intent(ACTION_PAIRING_STARTED).putExtra(EXTRA_PAIR_MAC, mac))
//
//        val device = scanningUtility.bluetoothAdapter.getRemoteDevice(mac)
//
//        BleConnectUtility.connectToDevice(this, device, mGattCallback)
//
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun stopPairing() {
//        sPairingMode = MODE_IDLE
//
//        gattToClose?.disconnect()
//        gattToClose?.close()
//
//        gattToClose = null
//        writtenData = null
//        Log.d(TAG, "stopPairing -> forceCloseGatt called")
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private val mGattCallback = object : BluetoothGattCallback() {
//
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            when (newState) {
//                BluetoothProfile.STATE_CONNECTED -> {
//
//                    Log.d(TAG, "mGattCallBack: connected -> discoverServices")
//                    gattToClose = gatt
//                    gatt.discoverServices()
//                }
//
//                BluetoothProfile.STATE_DISCONNECTED -> {
//
//                    Log.e(TAG, "mGattCallBack: disconnected status=$status mode =$sPairingMode")
//                    gattToClose = null
//                    if (sPairingMode != MODE_IDLE &&
//                        sPairingMode != MODE_DEVICE_PAIRING_COMMAND_WRITTEN
//                    ) {
//                        notifyUserDeviceNotConnected("Device disconnected unexpectedly")
//                    }
//                    sPairingMode = MODE_IDLE
//                }
//            }
//        }
//
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            if (status != BluetoothGatt.GATT_SUCCESS) {
//                notifyUserDeviceNotConnected("Service discovery failed status= $status")
//                return
//            }
//
//            sPairingMode = MODE_IDLE
//
//            Log.d(TAG, "onServicesDiscovered -> reading HW version")
//
//            readValueFromCharacteristic(
//                gatt,
//                BLE_SERVICE_TO_DEVICE_INFO,
//                BLE_CHARACTERISTIC_H_W_INFO
//            )
//        }
//
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            if (status != BluetoothGatt.GATT_SUCCESS) {
//                notifyUserDeviceNotConnected("Characteristic read failed status= $status")
//                return
//            }
//
//            val value = characteristic.value ?: return
//
//            when (characteristic.uuid) {
//
//                BLE_CHARACTERISTIC_H_W_INFO -> {
//
//                    val hw = BlePairingPacketHandler.getInfoFromByteArray(value)
//                    Log.d(TAG, "HW = $hw -> reading SW")
//                    readValueFromCharacteristic(
//                        gatt,
//                        BLE_SERVICE_TO_DEVICE_INFO,
//                        BLE_CHARACTERISTIC_S_W_INFO
//                    )
//                }
//
//
//                BLE_CHARACTERISTIC_S_W_INFO -> {
//
//                    val sw = BlePairingPacketHandler.getInfoFromByteArray(value)
//                    Log.d(TAG, "SW = $sw -> reading FW")
//                    readValueFromCharacteristic(
//                        gatt,
//                        BLE_SERVICE_TO_DEVICE_INFO,
//                        BLE_CHARACTERISTIC_F_W_INFO
//                    )
//                }
//
//                BLE_CHARACTERISTIC_F_W_INFO -> {
//
//                    val fw = BlePairingPacketHandler.getInfoFromByteArray(value)
//                    Log.d(TAG, "FW = $fw -> writing connect command 0x63 ")
//                    sPairingMode = MODE_SOFTWARE_READ_COMPLETED
//
//
//                    writeValueToCharacteristic(
//                        gatt,
//                        BLE_SERVICE_FOR_SECURED_PAIRING,
//                        BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//                        byteArrayOf(0x63)                                                   //-----0x63 = 99 = connect command-------------------
//                    )
//                }
//
//                BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA -> {
//
//                    Log.d(TAG, "Read f2f2f2f2 -> doSecureReadAction mode = $sPairingMode")
//
//                    doSecureReadAction(characteristic, gatt)
//                }
//            }
//        }
//
//
//        // onCharacteristicWrite()
//
//        override fun onCharacteristicWrite(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            if (status != BluetoothGatt.GATT_SUCCESS) {
//                Log.e(TAG, "Pairing write failed status =$status mode= $sPairingMode")
//                notifyUserDeviceNotConnected("Pairing write failed status =$status")
//                return
//            }
//            Log.d(TAG, "onCharacteristicWrite uuid = ${characteristic.uuid} mode = $sPairingMode")
//
//
//            when {
//                sPairingMode == MODE_DEVICE_PAIRING_COMMAND_WRITTEN -> {
//                    doUiCallBackForSuccess(gatt)
//                }
//
//                characteristic.uuid == BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA -> {
//
//                    doSecureCommandWriteAction(gatt, characteristic)
//                }
//
//                characteristic.uuid == BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA -> {
//
//                    readValueFromCharacteristic(
//                        gatt,
//                        BLE_SERVICE_FOR_SECURED_PAIRING,
//                        BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA
//                    )
//                }
//            }
//        }
//    }
//
//
//
//    @SuppressLint("MissingPermission")
//    private fun doSecureCommandWriteAction(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//
//        when(sPairingMode) {
//            MODE_SOFTWARE_READ_COMPLETED -> {
//
//                Log.d(TAG,"doSecureCommandWriteAction: MODE_SOFTWARE_READ_COMPLETED -> 0X01")
//                sPairingMode = MODE_TEST
//
//                writeValueToCharacteristic(
//                    gatt,
//                    BLE_SERVICE_FOR_SECURED_PAIRING,
//                    BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//                    byteArrayOf(0x01)
//                )
//            }
//
//            MODE_TEST -> {
//
//                Log.d(TAG,"doSecureCommandWriteAction: MODE_TEST -> sending security code packet")
//                sPairingMode = MODE_WRITE_SECURITY_CODE
//
//                val packet = BlePairingPacketHandler.generateSecurityCodePacket()
//                writtenData = packet
//
//                writeValueToCharacteristic(
//                    gatt,
//                    BLE_SERVICE_FOR_SECURED_PAIRING,
//                    BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA,
//                    packet
//                )
//            }
//
//            MODE_WRITE_FIRST_PAIRING_DATA -> {
//
//                Log.d(TAG,"doSecureCommandWriteAction: MODE_WRITE_FIRST_PAIRING_DATA -> sending first pairing data packet")
//
//                val sequenceNumber = BlePairingPacketHandler.createSequenceNumber()
//
//                val packet = BlePairingPacketHandler.createFirstPacket(
//                    macAddress ?: "",
//                    mDeviceIdAssigned,
//                    sequenceNumber
//                )
//
//                writtenData = packet
//
//                writeValueToCharacteristic(
//                    gatt,
//                    BLE_SERVICE_FOR_SECURED_PAIRING,
//                    BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA,
//                    packet
//                )
//            }
//
//            MODE_WRITE_SECOND_PAIRING_DATA -> {
//
//                Log.d(TAG,"doSecureCommandWriteAction: MODE_WRITE_SECOND_PAIRING_DATA -> sending second pairing data packet")
//
//                val sequenceNumber = BlePairingPacketHandler.createSequenceNumber()
//
//                val packet = BlePairingPacketHandler.createSecondPacket(
//                    mDeviceIdAssigned,
//                    sequenceNumber,
//                    1
//                )
//
//                writtenData = packet
//                sPairingMode = MODE_WRITE_COMPLETED
//
//                writeValueToCharacteristic(
//                    gatt,
//                    BLE_SERVICE_FOR_SECURED_PAIRING,
//                    BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_DATA,
//                    packet
//                )
//            }
//        }
//    }
//
//
//
//
//
//    @SuppressLint("MissingPermission")
//    private fun doSecureReadAction(characteristic: BluetoothGattCharacteristic, gatt: BluetoothGatt) {
//
//        val readValue = characteristic.value ?:return
//        val expected = writtenData ?: return
//
//        Log.v(TAG,"doSecureReadAction: sPairingMode = $sPairingMode")
//
//        when(sPairingMode) {
//
//            MODE_WRITE_SECURITY_CODE -> {
//
//                if (readValue.contentEquals(expected)) {
//                    Log.d(TAG,"doSecureReadAction: sec code verified -> writeFirstPairingData")
//                    writeFirstPairingData(gatt)
//                } else {
//                    Log.e(TAG,"doSecureReadAction: sec code MISMATCH")
//                    notifyUserDeviceNotConnected("Security code mismatch - device may not be  factory reset")
//                }
//            }
//
//            MODE_WRITE_FIRST_PAIRING_DATA -> {
//
//                if( readValue.contentEquals(expected)) {
//                    Log.d(TAG,"doSecureReadAction:sec code verified -> writeFirstPairingData")
//                    sPairingMode = MODE_WRITE_SECOND_PAIRING_DATA
//
//                    writeValueToCharacteristic(
//                        gatt,
//                        BLE_SERVICE_FOR_SECURED_PAIRING,
//                        BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//                        byteArrayOf(0x05)
//                    )
//                } else {
//                    Log.e(TAG,"doSecureReadAction: sec code MISMATCH")
//                    notifyUserDeviceNotConnected("FIRST PACKET MISMATCH")
//                }
//            }
//
//            MODE_WRITE_COMPLETED -> {
//
//                Log.d(TAG,"doSecureReadAction: MODE_WRITE_COMPLETED -> writepairingCloseCommand")
//                writepairingCloseCommand(gatt)
//            }
//        }
//    }
//
//
//    // ─── writeFirstPairingData() ──────────────────────────────────────────────
//
//
//    @SuppressLint("MissingPermission")
//    private fun writeFirstPairingData(gatt: BluetoothGatt) {
//        sPairingMode = MODE_WRITE_FIRST_PAIRING_DATA
//
//        Log.d(TAG,"writeFirstPairingDara: writing 0x03")
//        writeValueToCharacteristic(
//            gatt,
//            BLE_SERVICE_FOR_SECURED_PAIRING,
//            BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//            byteArrayOf(0x03)
//        )
//    }
//
//
//    // ─── writepairingCloseCommand() ───────────────────────────────────────────
//
//    @SuppressLint("MissingPermission")
//    private fun writepairingCloseCommand(gatt: BluetoothGatt) {
//        sPairingMode = MODE_DEVICE_PAIRING_COMMAND_WRITTEN
//
//        Log.d(TAG,"writepairingCloseCommand: writing 0x32")
//        writeValueToCharacteristic(
//            gatt,
//            BLE_SERVICE_FOR_SECURED_PAIRING,
//            BLE_CHARACTERISTIC_FOR_SECURED_PAIRING_COMMAND,
//            byteArrayOf(0x32)
//        )
//    }
//
//
//
//
//    // ─── doUiCallbackForSuccess() ─────────────────────────────────────────────
//    // EXACT NAME: doUiCallbackForSuccess(BluetoothGatt gatt)
//    // In Lumos: WiSeSecuredPairingService.doUiCallbackForSuccess()
//    //
//    // In Lumos:
//    //   private void doUiCallbackForSuccess(final BluetoothGatt gatt) {
//    //     isCompleted = true
//    //     sPairingMode = -1
//    //     DEVICE_COMMISSIONING_CALLBACK.pairingSuccess(mScanResult, pairedDevice)
//    //     closeGatt(gatt)
//
//    @SuppressLint("MissingPermission")
//    private fun doUiCallBackForSuccess(gatt: BluetoothGatt) {
//
//        Log.d(TAG,"doUiCallbackForSuccess: PAIRING SUCCESS  meshId = $mDeviceIdAssigned")
//
//        sPairingMode = MODE_IDLE
//
//        LocalBroadcastManager.getInstance(this)
//            .sendBroadcast(Intent(ACTION_PAIRING_SUCCESS).apply {
//                putExtra(EXTRA_PAIR_MESH_ID, mDeviceIdAssigned)
//                putExtra(EXTRA_PAIR_MAC, macAddress)
//            })
//
//        gatt.connect()
//    }
//
//
//
//    // ─── notifyUserDeviceNotConnected() ───────────────────────────────────────
//    // EXACT NAME: notifyUserDeviceNotConnected() in WiSeSecuredPairingService
//    // In Lumos takes int errorCode, we take String reason for clarity
//    //
//    // In Lumos:
//    //   DEVICE_COMMISSIONING_CALLBACK.pairingFailed(mScanResult, error)
//    //   forceCloseGatt(gattToClose)
//
//    @SuppressLint("MissingPermission")
//    private fun notifyUserDeviceNotConnected(reason: String) {
//
//        Log.e(TAG,"notifyUserDeviceNotConnected: $reason")
//
//        sPairingMode = MODE_IDLE
//        gattToClose?.disconnect()
//        gattToClose = null
//
//        LocalBroadcastManager.getInstance(this)
//            .sendBroadcast(Intent(ACTION_PAIRING_FAILED).apply {
//                putExtra(EXTRA_PAIR_ERROR, reason)
//                putExtra(EXTRA_PAIR_MAC, macAddress)
//            })
//    }
//
//
//    // ─── readValueFromCharacteristic() ────────────────────────────────────────
//    // EXACT NAME: readValueFromCharacteristic(BluetoothGatt, UUID, UUID)
//    // In Lumos: WiSeSecuredPairingService.readValueFromCharacteristic()
//    //
//    // In Lumos:
//    //   protected void readValueFromCharacteristic(final BluetoothGatt gatt, UUID service, UUID characteristic) {
//    //     BluetoothGattService bgService = gatt.getService(service)
//    //     bgCharacteristic = bgService.getCharacteristic(characteristic)
//    //     boolean state = gatt.readCharacteristic(bgCharacteristic)
//    //     if (!state) failureHandler(...)
//    //     else startTimerForFailure(...)
//
//
//    @SuppressLint("MissingPermission")
//    private fun readValueFromCharacteristic(gatt: BluetoothGatt,serviceUuid: UUID, characteristicUuid: UUID) {
//
//        val bgService = gatt.getService(serviceUuid) ?: run {
//            Log.e(TAG, "readValueFromCharacteristic: service not found$serviceUuid")
//            notifyUserDeviceNotConnected("Service not found $serviceUuid")
//            return
//        }
//
//        val bgCharacteristic = bgService.getCharacteristic(characteristicUuid) ?: run {
//            Log.e(TAG,"readValueFromCharacteristic: characteristic not found $characteristicUuid")
//            notifyUserDeviceNotConnected("Characteristic not found $characteristicUuid")
//            return
//        }
//
//        gatt.readCharacteristic(bgCharacteristic)
//    }




}





