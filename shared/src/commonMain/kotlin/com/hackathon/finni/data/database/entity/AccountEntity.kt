package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "account_state")
data class AccountEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "wallet_coins")
    val walletCoins: Int = 70,
    @ColumnInfo(name = "savings_coins")
    val savingsCoins: Int = 50,
    @ColumnInfo(name = "savings_goal_title")
    val savingsGoalTitle: String = "Астрономический телескоп",
    @ColumnInfo(name = "savings_goal_target")
    val savingsGoalTarget: Int = 200,
    @ColumnInfo(name = "savings_streak_days")
    val savingsStreakDays: Int = 0
)
