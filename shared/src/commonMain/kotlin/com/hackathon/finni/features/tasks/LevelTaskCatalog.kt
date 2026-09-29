package com.hackathon.finni.features.tasks

/** Связывает каждый узел карты с отдельным заданием. */
object LevelTaskCatalog {
    private val taskIdsByLevel = mapOf(
        1 to "first_envelopes",
        2 to "honest_work",
        3 to DilemmaCatalog.ROBOT_ID,
        4 to "shopping_list",
        5 to "reliable_ruler",
        6 to "check_notebook",
        7 to "healthy_lunch",
        8 to "healthy_snack",
        9 to "friend_gift",
        10 to "tv_ad",
        11 to "vending_machine",
        12 to "false_deal",
        13 to "backpack",
        14 to "garden_work",
        15 to "grandmother_gift",
        16 to "fair_audit",
        17 to "fair_accounts",
        18 to "new_horizons"
    )

    fun taskIdFor(levelId: Int): String? = taskIdsByLevel[levelId]
}
