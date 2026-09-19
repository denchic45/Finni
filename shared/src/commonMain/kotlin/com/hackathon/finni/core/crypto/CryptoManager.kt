package com.hackathon.finni.core.crypto

interface CryptoManager {
    fun encrypt(data: ByteArray): ByteArray
    fun decrypt(data: ByteArray): ByteArray
}
