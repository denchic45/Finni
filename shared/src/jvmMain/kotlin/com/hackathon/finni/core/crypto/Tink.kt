package com.hackathon.finni.core.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig

object Tink {
    fun createAead(): Aead {
        AeadConfig.register()
        val keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        return keysetHandle.getPrimitive(Aead::class.java)
    }
}
