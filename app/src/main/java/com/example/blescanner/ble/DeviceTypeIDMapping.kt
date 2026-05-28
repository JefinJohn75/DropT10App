package com.example.blescanner.ble


object DeviceTypeIDMapping {

    object TypeRange {
        const val NON_MESH_START   = 0
        const val NON_MESH_END     = 1000
        const val MESH_START       = 1000
        const val MESH_END         = 2000
        const val SENSOR_START     = 2000
        const val SENSOR_END       = 3000
        const val PLUG_START       = 3000
        const val PLUG_END         = 4000
        const val SHUTTER_START    = 4000
        const val SHUTTER_END      = 5000
        const val REMOTE_START     = 5000
        const val REMOTE_END       = 6000
        const val RELAY_START      = 6000
        const val RELAY_END        = 7000
        const val FAN_START        = 7000
        const val FAN_END          = 8000
        const val TAG_START        = 8000
        const val TAG_END          = 9000
        const val MIXER_START      = 9000
        const val MIXER_END        = 10000
        const val COOLER_START     = 10000
        const val COOLER_END       = 11000
        const val LISTENER_START   = 11000
        const val LISTENER_END     = 12000
        const val UART_START       = 12000
        const val UART_END         = 13000
        const val BRIDGE_START     = 13000
        const val BRIDGE_END       = 14000
        const val ACT_START        = 14000
        const val ACT_END          = 15000
    }

    // ── Device Type IDs — Mesh Lights ────────────────────────────────────
    object Mesh {
        const val LIGHT_BULB_NORMAL         = 1001
        const val LIGHT_BULB_TUBE           = 1002
        const val LED_STRIP                 = 1003
        const val T5_TUBE                   = 1005
        const val CFL                       = 1006
        const val RGB_BULB_OLD              = 1007
        const val DUAL_TONE_BULB            = 1008
        const val DUAL_TONE_RGB_BULB        = 1009
        const val DUAL_TONE_BULB_NEW        = 1010
        const val DUAL_TONE_RGB_BULB_NEW    = 1011
        const val LIGHT_BULB_NEW            = 1012
        const val T5_TUBE_NEW               = 1013
        const val RGB_BULB_NEW              = 1014
        const val DIMMABLE_BULB             = 1015
        const val CUSTOM_DEVICE             = 1016
        const val OSRAM_DEVICE              = 1017
        const val RGBW_LED                  = 1018
        const val DIMMER_CONTROL            = 1019
        const val DIMMER_DUAL_CONTROL       = 1020
        const val RGB_CCT_LAMP              = 1021
        const val CCT_TUBE                  = 1022
        const val WCA                       = 1023
        const val LFI_DIMMABLE_BULB         = 1024
        const val LFI_BULB                  = 1025
        const val WATER_HEATER              = 1026
        const val LFI_RGB_LED               = 1027
        const val SLM_D                     = 1029
        const val SLM_P                     = 1030
        const val CCT_RELAY                 = 1031
        const val ITM_MODULE                = 1032
        const val WCAG                      = 1033
        const val TRACK_LIGHT               = 1034
        const val WDA2CSL                   = 1035
        const val WCAD1                     = 1036
        const val WRP_DC_STRING             = 1037
        const val SMART_FAN                 = 1038
        const val TUNABLE_LIGHT             = 1042
        const val ROOM_CONTROLLER           = 1043
        const val FIXTURE_CONTROLLER        = 1044
        const val HELVAR_DRIVER             = 1045
        const val OMNI_TED                  = 1046
        const val LDRIVE                    = 1047
        const val WCM_450                   = 1048
        const val EMERGENCY_LIGHT           = 1049
        const val WCAD1N                    = 1051
        const val MULTICH_PWM               = 1052
        const val RGB_WHITE                 = 1053
        const val RADIAR_Z10                = 1056
        const val RGB_CCT                   = 1057
        const val FAN_CONTROLLER            = 1058
        const val CARTON_AT                 = 1059
        const val DOWNLIGHT                 = 1060
        const val DOWNLIGHT_RGB             = 1061
        const val GIMBAL_DOWNLIGHT          = 1062
        const val GIMBAL_DOWNLIGHT_RGB      = 1063
        const val E26_DOWNLIGHT             = 1064
        const val E26_DOWNLIGHT_RGB         = 1065
        const val BULB                      = 1066
        const val GIMBAL_SPOTLIGHT          = 1067
        const val GIMBAL_SPOTLIGHT_RGB      = 1068
        const val HVAC_GATEWAY              = 1069
        const val LDRIVE_E                  = 1073
        const val NREVOL_DIMMER             = 1081
        const val EMERGENCY_EXIT_TUBE       = 1082
        const val EM_EXIT_CONTROL_BOARD     = 1083
        const val CEILING_FAN               = 1501
        const val MICRO_SENSOR_PIR          = 1502
        const val MICRO_SENSOR_UWB          = 1503
        const val SSM_PIR                   = 1504
        const val POE_LAMP                  = 1505
        const val MC_WONG_SENSOR_CTRL       = 1506
        const val CYRUS_MD1                 = 1507
        const val RADIAR_ZP10               = 1508
        const val AIR_QUALITY_SENSOR        = 1509
    }

    // ── Device Type IDs — Sensors ─────────────────────────────────────────
    object Sensors {
        const val PIR                       = 2001
        const val SWITCH                    = 2002
        const val LDR                       = 2003
        const val SMOKE                     = 2004
        const val REMOTE_SERIAL             = 2005
        const val PIR_NEW                   = 2006
        const val LDR_NEW                   = 2007
        const val DOOR                      = 2008
        const val MULTI_SENSOR              = 2009
        const val DOOR_64K                  = 2010
        const val SENSOR_SWITCH             = 2011
        const val PARTICLE                  = 2012
        const val LEAKAGE                   = 2013
        const val SHUTTER_LDR               = 2014
        const val POWERED_AMBIENT_LDR       = 2015
        const val WIND                      = 2016
        const val PIR_TIMER                 = 2017
        const val ENOCEAN_SWITCH            = 2018
        const val SENSOR_BOX                = 2019
        const val WSAP3B                    = 2020
        const val WSA3B                     = 2021
        const val WSAPLI                    = 2022
        const val MCWONG_MOTION             = 2023
        const val SMOKE_DETECTOR            = 2024
        const val KINETIC_SWITCH            = 2025
        const val AC_SWITCH                 = 2026
        const val WMAP                      = 2027
        const val WMAM                      = 2028
        const val ENOCEAN_SENSOR            = 2029
        const val MCWONG_4_BUTTON           = 2030
        const val SWITCH_SIMPLE             = 2032
        const val MASTER_SWITCH             = 2033
        const val LOCK_SWITCH               = 2034
        const val KINETIC_ROTARY_SWITCH     = 2039
        const val DUAL_TECH_MOTION          = 2040
        const val HVAC_CONTROL_PANEL        = 2041
    }

    // ── Device Type IDs — Plugs ───────────────────────────────────────────
    object Plugs {
        const val TWO_PIN                   = 3001
        const val THREE_PIN                 = 3002
        const val TWO_PIN_NEW               = 3003
        const val THREE_PIN_NEW             = 3004
        const val SBC_INTERNAL              = 3005
        const val PMD                       = 3006
        const val SSO                       = 3007
        const val SMART_POWER_STRIP         = 3011
    }

    // ── Device Type IDs — Shutters ────────────────────────────────────────
    object Shutters {
        const val SHUTTER                   = 4001
        const val SHUTTER_NEW               = 4002
        const val CURTAIN_CONTROLLER        = 4003
        const val RADIAR_AIC                = 4004
    }

    // ── Device Type IDs — Remotes ─────────────────────────────────────────
    object Remotes {
        const val SHUTTER_REMOTE            = 5001
        const val SHUTTER_REMOTE_NEW        = 5002
        const val ACT_REMOTE                = 5003
        const val LIGHT_REMOTE              = 5004
        const val SWITCH_HUB                = 5005
        const val WSD4B                     = 5006
        const val WSC5B                     = 5007
        const val WIA4BR                    = 5008
    }

    // ── Device Type IDs — Relay ───────────────────────────────────────────
    object Relay {
        const val RELAY_DEVICE              = 6001
    }

    // ── Device Type IDs — Fan ─────────────────────────────────────────────
    object Fan {
        const val FAN_DEVICE                = 7001
    }

    // ── Device Type IDs — Tags ────────────────────────────────────────────
    object Tags {
        const val TAG                       = 8001
        const val ASSET_TAG                 = 8002
        const val SENSOR_TAG                = 8003
        const val MOTHER_TAG                = 8004
        const val MULTI_TAG                 = 8005
        const val I_AM_SAFE_TAG             = 8006
        const val LOGGER_CLIP               = 8007
        const val LOGGER_SIREN              = 8008
        const val LOGGER_WRISTBAND          = 8009
        const val LOGGER_WRISTBAND_VIB      = 8010
        const val LOGGER_SUPER_CUBE         = 8011
        const val LOGGER_BUTTON_TAG         = 8012
        const val ZONE_TAG                  = 8013
        const val ZONE_LOCATOR              = 8014
        const val PROXIMITY_BEACON          = 8015
        const val CONTACT_TRACING_BEACON    = 8016
        const val BEACON_LOGGER_TESTER      = 8017
        const val ALLSAFE_2430_CUBE         = 8018
        const val ALLSAFE_LR44_CUBE         = 8019
        const val ALLSAFE_2430_CHIP_ANT     = 8020
        const val ALLSAFE_2430_PCB_ANT      = 8021
        const val ALLSAFE_TEMP_SENSOR       = 8024
        const val TEMPERATURE_TAG           = 8026
    }

    // ── Device Type IDs — Mixer ───────────────────────────────────────────
    object Mixer {
        const val MIXER                     = 9001
        const val FOOD_PROCESSOR            = 9002
    }

    // ── Device Type IDs — Cooler ──────────────────────────────────────────
    object Cooler {
        const val COOLER                    = 10001
    }

    // ── Device Type IDs — Listener ────────────────────────────────────────
    object Listener {
        const val LISTENER                  = 11001
        const val PROXIMITY_LISTENER        = 11002
        const val DISPENSER_LISTENER        = 11003
    }

    // ── Device Type IDs — Wireless UART ──────────────────────────────────
    object WirelessUART {
        const val WIRELESS_UART             = 12001
    }

    // ── Device Type IDs — Bridge ──────────────────────────────────────────
    object Bridge {
        const val BRIDGE                    = 13001
        const val SBC_INTERNAL_BRIDGE       = 13002
        const val SBC_EXTERNAL_BRIDGE       = 13003
        const val SMWU                      = 13005
        const val TRACESAFE_BRIDGE          = 13006
        const val LTE_BRIDGE                = 13007
        const val ESP32_WIFI_BRIDGE         = 13009
        const val JIO_LTE_BRIDGE            = 13010
        const val ETHERNET_BRIDGE           = 13012
        const val XELEUM_BRIDGE             = 13013
        const val E_INK_TAG                 = 13014
    }

    // ── Device Type IDs — ACT ─────────────────────────────────────────────
    object ACT {
        const val RECEIVER                  = 14001
        const val CONTROLLER                = 14002
        const val VALVE                     = 14003
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get device name directly from scan record bytes.
     * Uses byte[12]+byte[13] as major, byte[14] as minor.
     * Exact port of DeviceTypeIDmapping.getDeviceTypeIDForMesh(byte[])
     */
    fun getDeviceNameFromScanRecord(scanData: ByteArray): String {
        val typeId = getDeviceTypeId(scanData)
        return if (typeId != -1) getDeviceName(typeId) else "WiSe Device"
    }

    /**
     * Get device type ID from scan record bytes.
     * byte[12] + byte[13] = major, byte[14] = minor
     */
    fun getDeviceTypeId(scanData: ByteArray): Int {
        if (scanData.size < 15) return -1
        val major = (scanData[12].toInt() and 0xFF) + (scanData[13].toInt() and 0xFF)
        val minor = scanData[14].toInt() and 0xFF
        return getDeviceTypeIdFromMajorMinor(major, minor)
    }

    /**
     * Get device name from type ID.
     * Exact port of DeviceTypeIDmapping.getDeviceName(int)
     */
    fun getDeviceName(id: Int): String {
        return when {
//            id > 14000 -> getACTReceiverName(id)
//            id > 13000 -> getBridgeName(id)
//            id > 12000 -> getWirelessUARTName(id)
//            id > 11000 -> getListenerName(id)
//            id > 10000 -> getCoolerName(id)
//            id > 9000  -> getMixerName(id)
//            id > 8000  -> getTagName(id)
//            id > 7000  -> getFanName(id)
//            id > 6000  -> getRelayName(id)
//            id > 5000  -> getRemoteName(id)
//            id > 4000  -> getShutterName(id)
//            id > 3000  -> getPlugName(id)
            id > 2000  -> getSensorName(id)
            id > 1000  -> getMeshName(id)
            else       -> getNonMeshName(id)
        }
    }


    private fun getDeviceTypeIdFromMajorMinor(major: Int, minor: Int): Int {
        return when (major) {
            1  -> getMeshTypeId(minor)
            2  -> getSensorTypeId(minor)
            3  -> getPlugTypeId(minor)
            4  -> getShutterTypeId(minor)
            5  -> getRemoteTypeId(minor)
            6  -> if (minor == 0) 6001 else -1
            7  -> if (minor == 0) 7001 else -1
            8  -> getTagTypeId(minor)
            9  -> getMixerTypeId(minor)
            10 -> if (minor == 0) 10001 else -1
            11 -> getListenerTypeId(minor)
            12 -> if (minor == 1) 12001 else -1
            13 -> getBridgeTypeId(minor)
            14 -> getACTTypeId(minor)
            15 -> getFSeriesTypeId(minor)
            else -> -1
        }
    }

    private fun getMeshTypeId(minor: Int): Int = when (minor) {
        0  -> 1001
        1  -> 1002
        2  -> 1003
        3, 4 -> 1005
        5  -> 1006
        6  -> 1007
        7  -> 1008
        8  -> 1009
        9  -> 1010
        10 -> 1011
        11 -> 1012
        12 -> 1013
        13 -> 1014
        14 -> 1015
        15 -> 1016
        16 -> 1017
        17 -> 1018
        18 -> 1019
        19 -> 1020
        20 -> 1021
        21 -> 1022
        22 -> 1023
        23 -> 1024
        24 -> 1025
        25 -> 1026
        26 -> 1027
        28 -> 1029
        29 -> 1030
        30 -> 1031
        31 -> 1032
        32 -> 1033
        33 -> 1034
        34 -> 1035
        35 -> 1036
        36 -> 1037
        37 -> 1038
        41 -> 1042
        42 -> 1043
        43 -> 1044
        44 -> 1045
        45 -> 1046
        46 -> 1047
        47 -> 1048
        48 -> 1049
        50 -> 1051
        51 -> 1052
        52 -> 1053
        55 -> 1056
        56 -> 1057
        57 -> 1058
        58 -> 1059
        59 -> 1060
        60 -> 1061
        61 -> 1062
        62 -> 1063
        63 -> 1064
        64 -> 1065
        65 -> 1066
        66 -> 1067
        67 -> 1068
        68 -> 1069
        72 -> 1073
        80 -> 1081
        81 -> 1082
        82 -> 1083
        else -> 1021
    }

    private fun getSensorTypeId(minor: Int): Int = when (minor) {
        0  -> 2001
        1  -> 2002
        2  -> 2003
        3  -> 2004
        6  -> 2006
        7  -> 2007
        8  -> 2008
        9  -> 2009
        10 -> 2010
        11 -> 2011
        12 -> 2012
        13 -> 2013
        14 -> 2014
        15 -> 2015
        16 -> 2016
        17 -> 2017
        18 -> 2018
        19 -> 2019
        20 -> 2020
        21 -> 2021
        22 -> 2022
        23 -> 2023
        24 -> 2024
        26 -> 2026
        27 -> 2027
        28 -> 2028
        30 -> 2030
        32 -> 2032
        33 -> 2033
        34 -> 2034
        39 -> 2039
        40 -> 2040
        41 -> 2041
        else -> -1
    }

    private fun getPlugTypeId(minor: Int): Int = when (minor) {
        1  -> 3001
        2  -> 3002
        3  -> 3003
        4  -> 3004
        5  -> 3005
        6  -> 3006
        7  -> 3007
        10 -> 3011
        else -> -1
    }

    private fun getShutterTypeId(minor: Int): Int = when (minor) {
        0  -> 4001
        1  -> 4002
        2  -> 4003
        3  -> 4004
        else -> -1
    }

    private fun getRemoteTypeId(minor: Int): Int = when (minor) {
        0  -> 5001
        1  -> 5002
        2  -> 5003
        3  -> 5004
        4  -> 5005
        5  -> 5006
        6  -> 5007
        7  -> 5008
        else -> -1
    }

    private fun getTagTypeId(minor: Int): Int = when (minor) {
        0  -> 8001
        1  -> 8002
        2  -> 8003
        3  -> 8004
        4  -> 8005
        5  -> 8006
        6  -> 8007
        7  -> 8008
        8  -> 8009
        9  -> 8010
        10 -> 8011
        11 -> 8012
        12 -> 8013
        13 -> 8014
        14 -> 8015
        15 -> 8016
        16 -> 8017
        17 -> 8018
        18 -> 8019
        19 -> 8020
        20 -> 8021
        23 -> 8024
        25 -> 8026
        else -> -1
    }

    private fun getMixerTypeId(minor: Int): Int = when (minor) {
        0  -> 9001
        1  -> 9002
        else -> -1
    }

    private fun getListenerTypeId(minor: Int): Int = when (minor) {
        0  -> 11001
        1  -> 11002
        2  -> 11003
        else -> -1
    }

    private fun getBridgeTypeId(minor: Int): Int = when (minor) {
        0  -> 13001
        1  -> 13002
        2  -> 13003
        4  -> 13005
        5  -> 13006
        6  -> 13007
        8  -> 13009
        9  -> 13010
        11 -> 13012
        12 -> 13013
        13 -> 13014
        else -> -1
    }

    private fun getACTTypeId(minor: Int): Int = when (minor) {
        0  -> 14001
        1  -> 14002
        2  -> 14003
        else -> -1
    }

    private fun getFSeriesTypeId(minor: Int): Int = when (minor) {
        1  -> 1501
        2  -> 1502
        3  -> 1503
        4  -> 1504
        5  -> 1505
        6  -> 1506
        7  -> 1507
        8  -> 1508
        9  -> 1509
        else -> -1
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE — Name resolution
    // ─────────────────────────────────────────────────────────────────────

    private fun getNonMeshName(id: Int): String = when (id) {
        1    -> "Wise Shutter"
        2    -> "Wise Light"
        3    -> "Wise LED Strip"
        4    -> "Wise Dimmable Light"
        else -> "WiSe Mesh Device"
    }

    private fun getMeshName(id: Int): String = when (id) {
        1001, 1002 -> "Light Bulb"
        1003 -> "WiSe LED Strip"
        1005 -> "Wise Dimmable Bulb"
        1006 -> "Wise CFL"
        1008 -> "Wise Dual Tone Bulb"
        1009 -> "Wise RGB Dual Tone Bulb"
        1010 -> "Wise Dual Tone Bulb"
        1011 -> "RM RGB Bulb"
        1012, 1025 -> "Wise Bulb"
        1013, 1015, 1024 -> "Wise Dimmable Bulb"
        1014 -> "Wise RGB Bulb"
        1016 -> "Custom Device"
        1017 -> "OSRAM Test Light"
        1018 -> "RGBW"
        1019 -> "WiSe Dimmer Control"
        1020 -> "Wise Dimmer Dual Control"
        1021 -> "RGB CCT Light"
        1022 -> "CCT Light"
        1023 -> "WCA"
        1026 -> "Water Heater"
        1027 -> "RGB Led"
        1029 -> "WCD"
        1030 -> "SLM P"
        1031 -> "CCT Relay Device"
        1032 -> "ITM Up"
        1033 -> "WCAG"
        1034 -> "Track Light"
        1035 -> "WDA2CSL"
        1036 -> "WCAD1"
        1037 -> "WRP-DC-String"
        1038 -> "SMART-FAN"
        1042 -> "Tunable Light"
        1043 -> "Room Controller"
        1044 -> "Fixture Controller"
        1045 -> "Helvar Driver"
        1046 -> "Omni TED"
        1047 -> "Ldrive"
        1048 -> "PSC-WCM-450-BLE"
        1049 -> "Emergency Light"
        1051 -> "WCAD1N"
        1052 -> "Multich PWM"
        1053 -> "RGB&W"
        1056 -> "Radiar Z10"
        1057 -> "RGB&CCT"
        1058 -> "Fan Controller"
        1059 -> "Carton AT"
        1060 -> "Downlight"
        1061 -> "Downlight with RGB Back Light"
        1062 -> "Gimbal Downlight"
        1063 -> "Gimbal Downlight with RGB Back Light"
        1064 -> "E26 Downlight"
        1065 -> "E26 Downlight with RGB Back Light"
        1066 -> "Bulb"
        1067 -> "Gimbal Recessed Spotlight"
        1068 -> "Gimbal Recessed Spotlight with RGB Back Light"
        1069 -> "HVAC Gateway"
        1073 -> "Ldrive E"
        1081 -> "15W Dimmable Driver"
        1082 -> "LSA Lamp"
        1083 -> "Main Board - EM"
        1501 -> "Ceiling Fan"
        1502 -> "Microsensor-PIR"
        1503 -> "Microsensor-UWB"
        1504 -> "WXD2CPLR"
        1505 -> "PoE Lamp"
        1506 -> "McWong Sensor Controller"
        1507 -> "Cyrus MD1"
        1508 -> "Radiar ZP10"
        1509 -> "Cyrus AQ"
        else -> "WiSe Mesh Device"
    }

    private fun getSensorName(id: Int): String = when (id) {
        2001 -> "PIR Sensor"
        2002 -> "Switch Sensor"
        2003 -> "LDR Sensor"
        2004 -> "Wise Smoke Sensor"
        2005 -> "Wise RM Remote Serial Sensor"
        2006 -> "Wise PIR Sensor"
        2007 -> "Wise LDR Sensor"
        2008 -> "Wise Door Sensor"
        2009 -> "Multi Sensor"
        2010 -> "Wise Door Sensor 64K"
        2011 -> "Wise Sensor Switch"
        2012 -> "Wise Particle Sensor"
        2013 -> "Wise Leakage Sensor"
        2014 -> "Wise Shutter LDR Sensor"
        2015 -> "Wise Powered Ambient LDR Sensor"
        2016 -> "Wise Wind Sensor"
        2017 -> "Wise PIR Timer Sensor"
        2018 -> "Enocean Switch"
        2019 -> "Sensor Box"
        2020 -> "WSAP3B"
        2021 -> "WSA3B"
        2022 -> "WSAPLI"
        2023 -> "Wise Sensor"
        2024 -> "Smoke Detector"
        2025 -> "Kinetic Switch"
        2026 -> "AC Switch"
        2027 -> "WMAP"
        2028 -> "WMAM"
        2029 -> "Enocean Sensor"
        2030 -> "PSC-DM-WS-400-BLE-WS"
        2032 -> "Switch"
        2033 -> "Master Switch"
        2034 -> "Lock Switch"
        2039 -> "Kinetic Rotary Switch"
        2040 -> "Dual Tech Motion Sensor"
        2041 -> "HVAC Control Panel"
        else -> "Wise Sensor"
    }
//
//    private fun getPlugName(id: Int): String = when (id) {
//        3001 -> "Wise Plug Two Pin"
//        3002 -> "Wise Plug 3 Pin"
//        3003 -> "Wise Plug"
//        3004 -> "Wise Plug Three Pin"
//        3005 -> "SBC-IWOB"
//        3006 -> "PMD"
//        3007, 3008, 3009, 3010 -> "Wise Plug"
//        3011 -> "Smart Power Strip"
//        else -> "Wise Plug"
//    }
//
//    private fun getShutterName(id: Int): String = when (id) {
//        4001 -> "Wise Shutter"
//        4002 -> "WiSe Shutter"
//        4003 -> "Curtain Controller"
//        4004 -> "Radiar AIC"
//        else -> "WiSe Shutter"
//    }
//
//    private fun getRemoteName(id: Int): String = when (id) {
//        5001, 5002 -> "Wise Shutter Remote"
//        5003 -> "ACT Remote"
//        5004 -> "Light Remote"
//        5005 -> "Switch Hub"
//        5006 -> "WSD4B"
//        5007 -> "WSC5B"
//        5008 -> "WIA4BR"
//        else -> "Wise Remote Device"
//    }
//
//    private fun getRelayName(id: Int): String = when (id) {
//        6001 -> "Wise Relay Device"
//        else -> "Wise Relay Device"
//    }
//
//    private fun getFanName(id: Int): String = when (id) {
//        7001 -> "Wise Fan"
//        else -> "Wise Fan Device"
//    }
//
//    private fun getTagName(id: Int): String = when (id) {
//        8001 -> "Wise Tag"
//        8002 -> "Wise Asset Tag"
//        8003 -> "Wise Sensor Tag"
//        8004 -> "Wise Mother Tag"
//        8005 -> "Multi Tag"
//        8006 -> "I Am Safe Tag"
//        8007 -> "Beacon Logger Clip"
//        8008 -> "Beacon Logger Siren"
//        8009 -> "Beacon Logger Wristband"
//        8010 -> "Beacon Logger Wristband With Vibrator"
//        8011 -> "Beacon Logger Super Cube"
//        8012 -> "Beacon Logger Button Tag"
//        8013 -> "Zone Tag"
//        8014 -> "Zone Locator"
//        8015 -> "Proximity Beacon"
//        8016 -> "Contact Tracing Control Beacon"
//        8017 -> "Beacon Logger Tester"
//        8018 -> "AllSafe 2430 Cube"
//        8019 -> "AllSafe LR44 Cube"
//        8020 -> "AllSafe 2430 Circular Tag (Chip Antenna)"
//        8021 -> "AllSafe 2430 Circular Tag (PCB Antenna)"
//        8024 -> "AllSafe Temperature Sensor Tag"
//        8026 -> "Temperature Tag"
//        else -> "Wise Tag Device"
//    }
//
//    private fun getMixerName(id: Int): String = when (id) {
//        9001 -> "Wise Mixer"
//        9002 -> "Wise Food Processor"
//        else -> "Wise Mixer Device"
//    }
//
//    private fun getCoolerName(id: Int): String = when (id) {
//        10001 -> "Wise Cooler Marine"
//        else -> "Wise Cooler Device"
//    }
//
//    private fun getListenerName(id: Int): String = when (id) {
//        11001 -> "Wise Listener"
//        11002 -> "Wise Proximity Listener"
//        11003 -> "Wise Dispenser Listener"
//        else -> "Wise Listener Device"
//    }
//
//    private fun getWirelessUARTName(id: Int): String = when (id) {
//        12001 -> "Wise Wireless UART"
//        else -> "Wise Wireless UART"
//    }
//
//    private fun getBridgeName(id: Int): String = when (id) {
//        13001 -> "Wise Bridge Device"
//        13002 -> "SBC-IB"
//        13003 -> "SBC-EB"
//        13005 -> "SMWU"
//        13006 -> "TraceSafe Bridge"
//        13007 -> "LTE Bridge"
//        13009 -> "ESP32 WiFi Bridge"
//        13010 -> "Jio LTE Bridge"
//        13012 -> "Ethernet Bridge"
//        13013 -> "Xeleum Bridge"
//        13014 -> "E-Ink Tags"
//        else -> "Wise Bridge"
//    }


}