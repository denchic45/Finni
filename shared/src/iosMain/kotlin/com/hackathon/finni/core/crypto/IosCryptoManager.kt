package com.hackathon.finni.core.crypto

class IosCryptoManager : CryptoManager {
    override fun encrypt(data: ByteArray): ByteArray = data
    override fun decrypt(data: ByteArray): ByteArray = data
}
