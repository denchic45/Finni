package com.hackathon.finni.data.storage

import androidx.datastore.core.okio.OkioSerializer
import com.hackathon.finni.core.crypto.CryptoManager
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

class AuthSettingsSerializer(
    private val cryptoManager: CryptoManager
) : OkioSerializer<AuthSettings> {
    override val defaultValue: AuthSettings = AuthSettings()

    override suspend fun readFrom(source: BufferedSource): AuthSettings {
        val bytes = source.readByteArray()
        if (bytes.isEmpty()) return defaultValue
        val decrypted = cryptoManager.decrypt(bytes)
        return try {
            Json.decodeFromString<AuthSettings>(decrypted.decodeToString())
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AuthSettings, sink: BufferedSink) {
        val json = Json.encodeToString(t)
        val encrypted = cryptoManager.encrypt(json.encodeToByteArray())
        sink.write(encrypted)
    }
}
