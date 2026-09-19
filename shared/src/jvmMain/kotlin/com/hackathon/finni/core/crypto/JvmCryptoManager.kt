package com.hackathon.finni.core.crypto

import com.google.crypto.tink.Aead

class JvmCryptoManager(private val aead: Aead) : CryptoManager {
    override fun encrypt(data: ByteArray): ByteArray {
        return aead.encrypt(data, null)
    }

    override fun decrypt(data: ByteArray): ByteArray {
        return aead.decrypt(data, null)
    }
}
