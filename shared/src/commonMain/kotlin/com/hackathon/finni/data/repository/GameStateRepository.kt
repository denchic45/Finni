package com.hackathon.finni.data.repository

import com.hackathon.finni.data.database.dao.AccountDao
import com.hackathon.finni.data.database.dao.LevelProgressDao
import com.hackathon.finni.data.database.dao.PetStateDao
import com.hackathon.finni.data.database.entity.AccountEntity
import com.hackathon.finni.data.database.entity.LevelProgressEntity
import com.hackathon.finni.data.database.entity.PetStateEntity
import com.hackathon.finni.features.levels.model.LevelItem
import com.hackathon.finni.features.levels.model.LevelStatus
import com.hackathon.finni.features.levels.model.LevelsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.min

interface GameStateRepository {
    val petState: Flow<PetStateEntity>
    val accountState: Flow<AccountEntity>
    val levels: Flow<List<LevelItem>>

    suspend fun feedPet(cost: Int = 10, hungerReplenish: Int = 1): Boolean
    suspend fun updateMood(mood: String)
    suspend fun addCoins(amount: Int)
    suspend fun spendCoins(amount: Int): Boolean
    suspend fun depositToSavings(amount: Int): Boolean
    suspend fun withdrawFromSavings(amount: Int): Boolean
    suspend fun advanceTimePhase()
    suspend fun completeLevel(levelId: Int, stars: Int, rewardCoins: Int)
    suspend fun resetGame()
}

class GameStateRepositoryImpl(
    private val petStateDao: PetStateDao,
    private val accountDao: AccountDao,
    private val levelProgressDao: LevelProgressDao
) : GameStateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val defaultPet = PetStateEntity(
        id = 1,
        name = "Финни",
        hunger = 4,
        mood = "Happy",
        timeTokens = 3,
        timePhase = "Day",
        currentChapter = 1,
        currentDay = 1
    )

    private val defaultAccount = AccountEntity(
        id = 1,
        walletCoins = 1150,
        savingsCoins = 50,
        savingsGoalTitle = "Астрономический телескоп",
        savingsGoalTarget = 200,
        savingsStreakDays = 0
    )

    init {
        scope.launch {
            seedIfNeeded()
        }
    }

    private suspend fun seedIfNeeded() {
        if (petStateDao.getPetState() == null) {
            petStateDao.upsertPetState(defaultPet)
        }
        if (accountDao.getAccountState() == null) {
            accountDao.upsertAccountState(defaultAccount)
        }
        if (levelProgressDao.getLevelsCount() == 0) {
            val entities = LevelsData.defaultLevels.map { it.toEntity() }
            levelProgressDao.upsertLevels(entities)
        }
    }

    override val petState: Flow<PetStateEntity> =
        petStateDao.observePetState().map { it ?: defaultPet }

    override val accountState: Flow<AccountEntity> =
        accountDao.observeAccountState().map { it ?: defaultAccount }

    override val levels: Flow<List<LevelItem>> =
        levelProgressDao.observeLevels().map { list ->
            if (list.isEmpty()) {
                LevelsData.defaultLevels
            } else {
                list.map { it.toModel() }
            }
        }

    override suspend fun feedPet(cost: Int, hungerReplenish: Int): Boolean {
        val account = accountDao.getAccountState() ?: defaultAccount
        val pet = petStateDao.getPetState() ?: defaultPet

        if (pet.hunger >= 6) return false
        if (account.walletCoins < cost) return false

        val newHunger = min(6, pet.hunger + hungerReplenish)
        accountDao.upsertAccountState(account.copy(walletCoins = account.walletCoins - cost))
        petStateDao.upsertPetState(pet.copy(hunger = newHunger, mood = "Happy"))
        return true
    }

    override suspend fun updateMood(mood: String) {
        val pet = petStateDao.getPetState() ?: defaultPet
        petStateDao.upsertPetState(pet.copy(mood = mood))
    }

    override suspend fun addCoins(amount: Int) {
        val account = accountDao.getAccountState() ?: defaultAccount
        accountDao.upsertAccountState(account.copy(walletCoins = account.walletCoins + amount))
    }

    override suspend fun spendCoins(amount: Int): Boolean {
        val account = accountDao.getAccountState() ?: defaultAccount
        if (account.walletCoins < amount) return false
        accountDao.upsertAccountState(account.copy(walletCoins = account.walletCoins - amount))
        return true
    }

    override suspend fun depositToSavings(amount: Int): Boolean {
        val account = accountDao.getAccountState() ?: defaultAccount
        if (account.walletCoins < amount) return false
        accountDao.upsertAccountState(
            account.copy(
                walletCoins = account.walletCoins - amount,
                savingsCoins = account.savingsCoins + amount
            )
        )
        return true
    }

    override suspend fun withdrawFromSavings(amount: Int): Boolean {
        val account = accountDao.getAccountState() ?: defaultAccount
        if (account.savingsCoins < amount) return false
        accountDao.upsertAccountState(
            account.copy(
                walletCoins = account.walletCoins + amount,
                savingsCoins = account.savingsCoins - amount
            )
        )
        return true
    }

    override suspend fun advanceTimePhase() {
        val pet = petStateDao.getPetState() ?: defaultPet
        val nextPhase = when (pet.timePhase) {
            "Morning" -> "Day"
            "Day" -> "Evening"
            "Evening" -> "Night"
            else -> "Morning"
        }
        val nextTokens = when (nextPhase) {
            "Morning" -> 3
            "Day" -> 2
            "Evening" -> 1
            else -> 0
        }
        petStateDao.upsertPetState(pet.copy(timePhase = nextPhase, timeTokens = nextTokens))
    }

    override suspend fun completeLevel(levelId: Int, stars: Int, rewardCoins: Int) {
        val currentLevel = levelProgressDao.getLevel(levelId) ?: return
        levelProgressDao.upsertLevel(
            currentLevel.copy(
                status = LevelStatus.COMPLETED.name,
                stars = maxOf(currentLevel.stars, stars)
            )
        )

        // Разблокируем следующий уровень
        val all = levelProgressDao.getAllLevels()
        val next = all.firstOrNull { it.number == currentLevel.number + 1 }
        if (next != null && next.status == LevelStatus.LOCKED.name) {
            levelProgressDao.upsertLevel(next.copy(status = LevelStatus.CURRENT.name))
        }

        // Начисляем монеты
        if (rewardCoins > 0) {
            addCoins(rewardCoins)
        }
    }

    override suspend fun resetGame() {
        petStateDao.upsertPetState(defaultPet)
        accountDao.upsertAccountState(defaultAccount)
        val entities = LevelsData.defaultLevels.map { it.toEntity() }
        levelProgressDao.upsertLevels(entities)
    }

    private fun LevelItem.toEntity(): LevelProgressEntity = LevelProgressEntity(
        id = id,
        number = number,
        title = title,
        description = description,
        chapterNumber = chapterNumber,
        chapterTitle = chapterTitle,
        status = status.name,
        stars = stars,
        rewardCoins = rewardCoins
    )

    private fun LevelProgressEntity.toModel(): LevelItem = LevelItem(
        id = id,
        number = number,
        title = title,
        description = description,
        chapterNumber = chapterNumber,
        chapterTitle = chapterTitle,
        status = try {
            LevelStatus.valueOf(status)
        } catch (_: Exception) {
            LevelStatus.LOCKED
        },
        stars = stars,
        rewardCoins = rewardCoins
    )
}
