package com.example.blescanner.ble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
        const val START_CONNECT          = "START_CONNECT"
        const val STOP_CONNECT           = "STOP_CONNECT"
        const val ACTION_CONNECT_SUCCESS = "com.example.blescanner.CONNECT_SUCCESS"
        const val ACTION_CONNECT_FAILED  = "com.example.blescanner.CONNECT_FAILED"
        const val ACTION_CONNECT_STOPPED = "com.example.blescanner.CONNECT_STOPPED"

        const val START_OTA              = "START_OTA"
        const val ACTION_OTA_PROGRESS    = "com.example.blescanner.OTA_PROGRESS"
        const val ACTION_OTA_COMPLETE    = "com.example.blescanner.OTA_COMPLETE"
        const val ACTION_OTA_FAILED      = "com.example.blescanner.OTA_FAILED"

        const val EXTRA_OTA_PROGRESS     = "extra_ota_progress"
        const val EXTRA_OTA_ERROR        = "extra_ota_error"
        const val EXTRA_OTA_FILE_PATH    = "extra_ota_file_path"

        const val EXTRA_MAC_ADDRESS = "extra_mac_address"
        const val EXTRA_CONNECT_ERROR = "extra_connect_error"
        const val EXTRA_BLE_DEVICE = "extra_ble_device"
        const val EXTRA_WITH_TIMER = "extra_with_timer"

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


    val DROP_T1_SERVICE_UUID = UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB")

    val DROP_T1_CHAR_UUID    = UUID.fromString("0000DDDD-0000-1000-8000-00805F9B34FB")

    private var connectGatt: BluetoothGatt? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var scanTimerJob: Job? = null
    private var isScanning = false
    private lateinit var scanningUtility: BleScanningUtility

    private var otaBytes: ByteArray? = null
    private var otaCurrentIndex = 0
    private var otaMacAddress = ""
    private var mMtuValueChunkSize = 16

    private var dropT1OtaState   = DROP_T1_STATE_UNKNOWN
    private var dropT1AllChunkSent = false
    private var dropT1LastCmd: Int = 0
    private val DROP_T1_REQUEST_MTU = 244

    private lateinit var connectGattCallBack: BluetoothGattCallback






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
                Log.i(TAG, "OTA clicked: $mac file= $filePath")
                startDropT1Ota(mac,filePath)
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
    override fun onCreate() {
        super.onCreate()
        scanningUtility = BleScanningUtility(this)
        createNotificationChannel()
        connectGattCallBack = object : BluetoothGattCallback() {


//    private val connectGattCallBack: BluetoothGattCallback = object : BluetoothGattCallback() {

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
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    Log.d(TAG, "connectGattCallback: DISCONNECTED || DISCONNECTED || DISCONNECTED || DISCONNECTED")
                    val mac = gatt.device.address
                    gatt.close()
                    connectGatt = null


                    if (dropT1OtaState == DROP_T1_STATE_DOWNLOADED) {
                        Log.i(TAG,"DropT1: disconnected after SWAP -> reconnecting in 1s")
                        serviceScope.launch {
                            delay(1000L)
                            val device = scanningUtility.bluetoothAdapter.getRemoteDevice(mac)
                            BleConnectUtility.connectToDevice(
                                this@BleScanService,device,connectGattCallBack
                            )
                        }
                        return
                    }

                    otaBytes = null
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

            val attPayLoad = minOf(mtu,DROP_T1_REQUEST_MTU) - 3
            mMtuValueChunkSize = attPayLoad - 1


            Log.i(TAG, "OTA: MTU agreed = $mtu attPayLoad = $attPayLoad chunk size = $mMtuValueChunkSize")
            gatt?.discoverServices()

        }



        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "connectGattCallback: SERVICES DISCOVERED -> ready!")


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


                DROP_T1_CHAR_UUID -> {

                    val (state,returnCode,version) = DropT1StateResponse(value)

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
        }


        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
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

                DROP_T1_CHAR_UUID -> {

                    Log.i(TAG, "DropT1: write complete cmd=0x${"%02X".format(dropT1LastCmd)}")

                    when (dropT1LastCmd) {

                        DROP_T1_CMD_INITIATE -> {
                            Log.i(TAG, "DropT1: INITIATE confirmed → reading state")
                            serviceScope.launch {
                                delay(300L)
                                val g = connectGatt ?: return@launch
                                readDropT1State(g)
                            }
                        }

                        DROP_T1_CMD_TRANSFER -> {
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
                            Log.i(TAG, "DropT1: SWAP confirmed → device rebooting")
                        }

                        DROP_T1_CMD_CONFIRM -> {
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
        Log.d(TAG, "Service created")
    }


    private fun DropT1StateResponse (value : ByteArray) : Triple<Int , Int , String> {

        Log.i(TAG, "DropT1 raw bytes: ${value.take(9).map { "0x%02X".format(it) }}")


        val state      = value[0].toInt() and 0xFF
        val returnCode = ((value[1].toInt() and 0xFF) shl 24) or
                        ((value[2].toInt() and 0xFF) shl 16) or
                        ((value[3].toInt() and 0xFF) shl 8)  or
                        (value[4].toInt() and 0xFF)

        val major      = value[5].toInt() and 0xFF
        val minor      = value[6].toInt() and 0xFF
        val patch      = value[7].toInt() and 0xFF
        val build      = value[8].toInt() and 0xFF
        val version    = "$major.$minor.$patch.$build"

        Log.i(TAG,"DropT1 parsed: state = $state returnCode = $returnCode version = $version")
        return Triple(state, returnCode, version)
    }




    @SuppressLint("MissingPermission")
    private fun startDropT1Ota(mac:String, filePath:String) {

        val  file = java.io.File(filePath)
        if (!file.exists()) {
            broadcastOtaFailed("File not found: $filePath", mac)
            return
        }

        val bytes = file.readBytes()
        if (bytes.isEmpty()) {
            broadcastOtaFailed("File is empty",mac)
            return
        }
        otaBytes = bytes
        otaCurrentIndex = 0
        otaMacAddress = mac
        dropT1AllChunkSent = false

        Log.i(TAG,"DropT1 OTA: file loaded ${bytes.size} bytes -> sending INITIATE")

        val gatt = connectGatt ?: run {
            broadcastOtaFailed("Not connected",mac)
            return
        }
        val packet  = buildInitiatePacket(bytes)
        val success = writeDropT1Char(gatt, packet)

        if (!success) {
            broadcastOtaFailed("INITIATE write failed", mac)
        }
    }



    private fun buildInitiatePacket(fwBytes: ByteArray): ByteArray {

        val length = fwBytes.size
        Log.i(TAG,"DropT1 OTA: file size || file size || file size = $length")

        var checksum = 0
        for (b in fwBytes) {
            checksum +=  (b.toInt() and 0xFF)
        }

        val packet = ByteArray(6)
        packet[0]  = DROP_T1_CMD_INITIATE.toByte()
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

        dropT1LastCmd  = data[0].toInt() and 0xFF
        char.value = data
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

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

        gatt.readCharacteristic(char)
        Log.d(TAG,"DropT1: readChar triggered")
    }


    @SuppressLint("MissingPermission")
    private fun sendNextDropT1Chunk() {

        val all = otaBytes ?:return
        val gatt = connectGatt ?: run {
            broadcastOtaFailed("GATT lost", otaMacAddress)
            return
        }

        if (otaCurrentIndex >= all.size) {
            dropT1AllChunkSent = true
            Log.i(TAG, "DropT1: all chunks sent → waiting for onCharacteristicWrite to confirm")
            serviceScope.launch {
                delay(1000L)
                val g = connectGatt ?: return@launch
                readDropT1State(g)
            }
            return
        }

        val end = minOf(otaCurrentIndex + mMtuValueChunkSize,all.size)
        val chunk = all.copyOfRange(otaCurrentIndex,end)
        otaCurrentIndex = end


        val packet = ByteArray(1+ chunk.size)
        packet[0]  = DROP_T1_CMD_TRANSFER.toByte()
        chunk.copyInto(packet,1)


        val progress = otaCurrentIndex.toFloat() / all.size.toFloat() * 100f
        Log.d(TAG, "DropT1 CHUNK: $otaCurrentIndex/${all.size} (${"%.1f".format(progress)}%)")
        broadcastOtaProgress(progress,otaMacAddress)

        writeDropT1Char(gatt,packet)

    }


    @SuppressLint("MissingPermission")
    private fun sendDropT1Swap() {
        val gatt = connectGatt ?:return
        val packet = byteArrayOf(DROP_T1_CMD_SWAP.toByte())
        Log.i(TAG,"DropT1: sending SWAP [0X03]")
        writeDropT1Char(gatt,packet)
    }

    @SuppressLint("MissingPermission")
    private fun sendDropT1Confirm() {
        val gatt = connectGatt ?:return
        val packet = byteArrayOf(DROP_T1_CMD_CONFIRM.toByte())
        Log.i(TAG,"DropT1: sending CONFIRM [0X04]")
        writeDropT1Char(gatt, packet)
    }





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
        Log.i(TAG, "Connect success: $mac")

        LocalBroadcastManager.getInstance(this@BleScanService)
            .sendBroadcast(Intent(ACTION_CONNECT_SUCCESS).apply {
                putExtra(EXTRA_MAC_ADDRESS, mac)
            })

    }

}





