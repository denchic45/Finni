package com.hackathon.finni.features.levels.model

data class LevelItem(
    val id: Int,
    val number: Int,
    val title: String,
    val description: String = "",
    val chapterNumber: Int = 1,
    val chapterTitle: String = "",
    val status: LevelStatus = LevelStatus.LOCKED,
    val stars: Int = 0,
    val rewardCoins: Int = 25
)
