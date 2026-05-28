package com.example.blescanner.ble

import android.util.Log
import java.util.Arrays

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

class RC5 {
    val TAG: String = this.javaClass.getSimpleName()
    var _wordLengthInBit: Char = 16.toChar()
    var _round: Char = '\u000c'
    var _keyLengthInByte: Char = 16.toChar()
    var _keyLengthInWord: Char = '\b'
    var _sTableSize: Char = 26.toChar()
    var _S: CharArray
    var _P: Char
    var _Q: Char

    init {
        this._S = CharArray(this._sTableSize.code)
        this._P = '럡'
        this._Q = '鸷'
    }

    fun cyclicRightShift(x: Char, y: Char): Char {
        return (x.code shr (y.code and this._wordLengthInBit.code - 1) or (x.code shl this._wordLengthInBit.code - (y.code and this._wordLengthInBit.code - 1))).toChar()
    }

    fun cyclicLeftShift(x: Char, y: Char): Char {
        return (x.code shl (y.code and this._wordLengthInBit.code - 1) or (x.code shr this._wordLengthInBit.code - (y.code and this._wordLengthInBit.code - 1))).toChar()
    }

    fun rc5KeyIninitialize(keyData: CharArray) {
        val u = 2
        val L = CharArray(8)
        Arrays.fill(L, '\u0000')

        for (i in 15 downTo -1 + 1) {
            L[i / u] = ((L[i / u].code shl 8) + keyData[i].code).toChar()
        }

        var var9 = 1

        this._S[0] = this._P
        while (var9 < this._sTableSize.code) {
            this._S[var9] = (this._S[var9 - 1].code + this._Q.code).toChar()
            ++var9
        }

        var A = 0.toChar()
        var B = 0.toChar()
        var9 = 0
        var j = 0

        for (k in 0..<3 * this._sTableSize.code) {
            this._S[var9] =
                this.cyclicLeftShift((this._S[var9].code + A.code + B.code).toChar(), '\u0003')
            A = this._S[var9]
            L[j] = this.cyclicLeftShift(
                (L[j].code + A.code + B.code).toChar(),
                (A.code + B.code).toChar()
            )
            B = L[j]
            var9 = (var9 + 1) % this._sTableSize.code
            j = (j + 1) % this._keyLengthInWord.code
        }
    }

    fun rc5Encrypt(data: ByteArray): ByteArray {
        var inputLog = ""

        for (b in data) {
            inputLog = inputLog + String.format("%02X", b.toInt() and 255)
        }

        Log.d(this.TAG, "rc5Encrypt() : input >> " + inputLog)
        val char1 =
            ((data[0].toInt() and 255 or ((255 and data[1].toInt()) shl 8)) and '\uffff'.code).toChar()
        val char2 =
            ((data[2].toInt() and 255 or ((255 and data[3].toInt()) shl 8)) and '\uffff'.code).toChar()
        val input = charArrayOf(char1, char2)
        var B = (input[1].code + this._S[1].code).toChar()
        var A = (input[0].code + this._S[0].code).toChar()

        var i = 1
        while (i <= this._round.code) {
            A = (this.cyclicLeftShift(
                (A.code xor B.code).toChar(),
                B
            ).code + this._S[2 * i].code).toChar()
            B = (this.cyclicLeftShift(
                (B.code xor A.code).toChar(),
                A
            ).code + this._S[2 * i + 1].code).toChar()
            ++i
        }

        val ct = charArrayOf(A, B)
        val out = ByteArray(4)
        val tempChar0 = ct[0]
        val tempChar1 = ct[1]
        out[0] = (tempChar0.code and 255).toByte()
        out[1] = (tempChar0.code shr 8 and 255).toByte()
        out[2] = (tempChar1.code and 255).toByte()
        out[3] = (tempChar1.code shr 8 and 255).toByte()
        var outputLog = ""

        for (b in out) {
            outputLog = outputLog + String.format("%02X", b.toInt() and 255)
        }

        Log.d(this.TAG, "rc5Encrypt() : output >> " + outputLog)
        return out
    }

    fun rc5Decrypt(ct: CharArray): CharArray {
        val pt = CharArray(2)
        var B = ct[1]
        var A = ct[0]

        for (i in this._round.code downTo 1) {
            B = (this.cyclicRightShift(
                (B.code - this._S[2 * i + 1].code).toChar(),
                A
            ).code xor A.code).toChar()
            A = (this.cyclicRightShift(
                (A.code - this._S[2 * i].code).toChar(),
                B
            ).code xor B.code).toChar()
        }

        pt[1] = (B.code - this._S[1].code).toChar()
        pt[0] = (A.code - this._S[0].code).toChar()
        return pt
    }

    fun rc5Decrypt(data: ByteArray): ByteArray {
        val pt = CharArray(2)
        val char1 =
            ((data[0].toInt() and 255 or ((255 and data[1].toInt()) shl 8)) and '\uffff'.code).toChar()
        val char2 =
            ((data[2].toInt() and 255 or ((255 and data[3].toInt()) shl 8)) and '\uffff'.code).toChar()
        var B = char2
        var A = char1

        for (i in this._round.code downTo 1) {
            B = (this.cyclicRightShift(
                (B.code - this._S[2 * i + 1].code).toChar(),
                A
            ).code xor A.code).toChar()
            A = (this.cyclicRightShift(
                (A.code - this._S[2 * i].code).toChar(),
                B
            ).code xor B.code).toChar()
        }

        pt[1] = (B.code - this._S[1].code).toChar()
        pt[0] = (A.code - this._S[0].code).toChar()
        val out = ByteArray(4)
        val tempChar0 = pt[0]
        val tempChar1 = pt[1]
        out[0] = (tempChar0.code and 255).toByte()
        out[1] = (tempChar0.code shr 8 and 255).toByte()
        out[2] = (tempChar1.code and 255).toByte()
        out[3] = (tempChar1.code shr 8 and 255).toByte()
        var outputLog = ""

        for (b in out) {
            outputLog = outputLog + String.format("%02X", b.toInt() and 255)
        }

        Log.d(this.TAG, "rc5Decrypt() : output >> " + outputLog)
        return out
    }
}