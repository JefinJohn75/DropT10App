package com.example.blescanner.ble

import java.security.SecureRandom


object BlePairingPacketHandler {

    private var networkKey: ByteArray = ByteArray(16)
    private var networkId: Long = 1L
    private var sourceId: Int = 1

    fun init() {
        networkKey = ByteArray(16).also { SecureRandom().nextBytes(it)}
        networkId = (SecureRandom().nextInt(65000) + 1).toLong()
        sourceId = SecureRandom().nextInt(254) + 1
    }

    fun getNetworkKey() = networkKey
    fun getNetworkId() = networkId
    fun getSourceId() = sourceId

    private fun calculateCrc(data: ByteArray): ByteArray {
        var crc = 0xFFFF

        for(b in data) {

            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }

        return byteArrayOf(
            ((crc shr 8) and 0xFF).toByte(),
            (crc and 0xFF).toByte()
        )
    }



    private fun getPacket(crcData: ByteArray): ByteArray {

        val crc = calculateCrc(crcData)

        val packet = ByteArray(16)

        packet[0] = crc[0]
        packet[1] = crc[1]

        for (i in crcData.indices) {
            packet[i + 2] = crcData[i]
        }
        return packet
    }


    fun generateSecurityCodePacket(
        currentSecurityCode: Int = 0,
        newSecurityCode: Int = 0
    ): ByteArray {
        val crcData = ByteArray(4)

        crcData[6] = (currentSecurityCode and 0xFF).toByte()
        crcData[7] = ((currentSecurityCode shr 8) and 0xFF).toByte()

        crcData[8] = (newSecurityCode and 0xFF).toByte()
        crcData[9] = ((newSecurityCode shr 8) and 0xFF).toByte()
        return getPacket(crcData)
    }


    fun generateCrcDataForFirstPacket(
        deviceMac: String,
        meshId: Int,
        sequenceNumber: Int,
    ): ByteArray {

        val macBytes = macToByteArray(deviceMac)
        val uuidBytes = ByteArray(16)
        for (i in 0..5) {
            uuidBytes[10 + i] = macBytes[i]
        }

        val twoByteSourceId = intToTwoBytes(meshId)

        val networkIdBytes = longToTwoBytes(networkId)

        val crcData = ByteArray(14)
        var counter = 0

        crcData[counter++] = sequenceNumber.toByte()

        crcData[counter++] = twoByteSourceId[1]

        crcData[counter++] = networkIdBytes[0]
        crcData[counter++] = networkIdBytes[1]

        for(j in 10..15) {
            crcData[counter++] = uuidBytes[j]
        }

        for(j in 0..3) {
            crcData[counter++] = networkKey[j]
        }
        return crcData
    }



    // ─── createFirstPacket() ──────────────────────────────────────────────────
    // EXACT NAME from Lumos:
    //   WiSeSecuredPairingPacketHandler.createFirstPacket(WiSeScanResult, int meshId)
    //
    // This is the unencrypted version (no pairingKey from server)
    // In Lumos: createFirstSecuredPacket() is the encrypted version
    // We use createFirstPacket() = unencrypted


    fun  createFirstPacket(
        deviceMac: String,
        meshId: Int,
        sequenceNumber: Int
    ): ByteArray {

        val crcData = generateCrcDataForFirstPacket(deviceMac, meshId, sequenceNumber)

        return getPacket(crcData)
    }


    // ─── generateCrcDataForSecondPacket() ────────────────────────────────────
    // EXACT NAME from Lumos:
    //   WiSeSecuredPairingPacketHandler.generateCrcDataForSecondPacket()
    //
    // What this tells the device:
    // - Confirms the mesh ID
    // - Whether device should stay BLE connectable
    // - Rest of the network key (bytes 4-15)
    //
    // Payload structure (crcData = 14 bytes):
    // [0]     = sequenceNumber (same as first packet)
    // [1]     = (meshId highByte & 0x1F) | (0x80 if connectable)
    //           0x1F = 0001 1111 = lower 5 bits of high byte
    //           0x80 = 1000 0000 = connectable flag bit
    // [2..13] = networkKey[4..15]  (remaining 12 bytes of key)

    fun generateCrcDataForSecondPacket(
        meshId: Int,
        sequenceNumber: Int,
        isConnectible: Int = 1
    ): ByteArray {

        val twoBytesSourceId = intToTwoBytes(meshId)

        var deviceIdConnectableCombo = (twoBytesSourceId[0].toInt() and 0x1F).toByte()

        if(isConnectible == 1) {
            deviceIdConnectableCombo = (deviceIdConnectableCombo.toInt() or 0x80).toByte()
        }

        val crcData = ByteArray(14)
        var counter = 0

        crcData[counter++] = sequenceNumber.toByte()

        crcData[counter++] = deviceIdConnectableCombo

        for (j in 4..15) {
            crcData[counter++] = networkKey[j]
        }

        return crcData
    }


    // ─── createSecondPacket() ─────────────────────────────────────────────────
    // EXACT NAME from Lumos:
    //   WiSeSecuredPairingPacketHandler.createSecondPacket(WiSeScanResult, int deviceId, int isConnectible)
    //
    // Unencrypted version (no pairingKey)

    fun createSecondPacket(
        meshId: Int,
        sequenceNumber: Int,
        isConnectible: Int = 1
    ): ByteArray {

        val crcData = generateCrcDataForSecondPacket(meshId, sequenceNumber, isConnectible)

        return getPacket(crcData)
    }



    // ─── createRandomMeshId() ─────────────────────────────────────────────────
    // EXACT NAME from Lumos:
    //   DeviceCommissionViewModel.createRandomMeshId()
    //
    // Rules from Lumos source:
    // - Must be 1-4094
    // - Neither byte can be 0, 127, 128, or 255
    //   (these are reserved addresses in Wisilica mesh protocol)

    fun createRandomMeshId(): Int {
        val invalidBytes = setOf(0,127,128,255)

        var id: Int
        do {
            id = SecureRandom().nextInt(4094) + 1
        } while (

            (id and 0xFF) in invalidBytes ||
            ((id shr 8) and 0xFF) in invalidBytes

        )
        return id
    }


    // ─── getInfoFromByteArray() ───────────────────────────────────────────────
    //   WiSeSecuredPairingService.getInfoFromByteArray(byte[] array)
    //   ByteUtility.getVersionFromByteArray(array)
    //
    // Converts 3 bytes to version string
    // e.g. bytes [2, 4, 71] → "2.4.71"

    fun getInfoFromByteArray(bytes: ByteArray): String {
        if (bytes.size < 3) return "unknown"

        return "${bytes[0].toInt() and 0xFF}" +
                ".${bytes[1].toInt() and 0xFF}" +
                ".${bytes[2].toInt() and 0xFF}"

    }


    // In Lumos: SecureRandom().nextInt(250) + 1
    //   in WiSeSecuredPairingService.setPairingDeviceData()
    //   "int sequenceNumber = random.nextInt(250) + 1"

    fun createSequenceNumber(): Int = SecureRandom().nextInt(250) + 1


    private fun intToTwoBytes(value: Int): ByteArray = byteArrayOf(
        ((value shr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )


    private fun longToTwoBytes(value:Long): ByteArray = byteArrayOf(
        ((value shr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )


    private fun macToByteArray(mac: String): ByteArray {
        val cleanMac = mac.replace(":", "").replace("-", "")

        val result = ByteArray(cleanMac.length / 2)

        for ( i in result.indices) {
            val hexByte = cleanMac.substring(i * 2, i * 2 + 2)

            result[i] = hexByte.toInt(16).toByte()
        }
        return result
    }

}