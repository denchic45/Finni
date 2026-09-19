package com.hackathon.finni.data.sync

import com.hackathon.finni.core.presentation.error.Failure
import kotlinx.serialization.Serializable

@Serializable
enum class ActionType {
    CREATE, UPDATE, DELETE
}

@Serializable
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNC_FAILED
}

sealed interface SyncResult {
    data object Synced : SyncResult
    data class Queued(val failure: Failure) : SyncResult
    data class Failed(val failure: Failure) : SyncResult
}
