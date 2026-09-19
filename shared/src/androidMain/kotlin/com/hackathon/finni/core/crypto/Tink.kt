package com.hackathon.finni.core.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

object Tink {
    fun createAead(context: Context): Aead {
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_key_file")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://tink_master_key")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }
}
