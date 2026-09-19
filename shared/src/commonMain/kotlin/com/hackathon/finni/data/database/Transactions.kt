package com.hackathon.finni.data.database

import androidx.room3.RoomDatabase
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection

suspend fun <T> RoomDatabase.withTransaction(
    block: suspend () -> T
) = useWriterConnection { transactor ->
    transactor.immediateTransaction {
        block()
    }
}
