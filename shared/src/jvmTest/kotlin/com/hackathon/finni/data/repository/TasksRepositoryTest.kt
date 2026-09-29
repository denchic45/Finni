package com.hackathon.finni.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.entity.AccountEntity
import com.hackathon.finni.data.database.entity.PetStateEntity
import com.hackathon.finni.features.tasks.DilemmaCatalog
import com.hackathon.finni.features.tasks.TaskBlockReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TasksRepositoryTest {
    private fun openDatabase(path: String): AppDatabase =
        Room.databaseBuilder<AppDatabase>(name = path).setDriver(BundledSQLiteDriver()).build()

    @Test
    fun versionOneMigrationPreservesExistingGame() = runBlocking<Unit> {
        val path = Files.createTempDirectory("finni-migration-test").resolve("game.db").toString()
        val schemaPath = "schemas/com.hackathon.finni.data.database.AppDatabase/1.json"
        val file = listOf(File(schemaPath), File("shared/$schemaPath")).first { it.isFile }
        val schema = Json.parseToJsonElement(file.readText()).jsonObject.getValue("database").jsonObject
        BundledSQLiteDriver().open(path).use { connection ->
            schema.getValue("entities").jsonArray.forEach { entity ->
                val table = entity.jsonObject
                val sql = table.getValue("createSql").jsonPrimitive.content.replace(
                    "\${TABLE_NAME}", table.getValue("tableName").jsonPrimitive.content
                )
                connection.prepare(sql).use { it.step() }
            }
            schema.getValue("setupQueries").jsonArray.forEach { query ->
                connection.prepare(query.jsonPrimitive.content).use { it.step() }
            }
            connection.prepare("INSERT INTO account_state VALUES (1, 70, 50, 'Telescope', 200, 2)").use { it.step() }
            connection.prepare("INSERT INTO pet_state VALUES (1, 'Finni', 4, 'Neutral', 3, 'Morning', 1, 1)").use { it.step() }
            connection.prepare("PRAGMA user_version = 1").use { it.step() }
        }
        val database = openDatabase(path)
        try {
            assertEquals(70, database.accountDao().getAccountState()?.walletCoins)
            assertEquals(50, database.accountDao().getAccountState()?.savingsCoins)
            assertEquals(3, database.petStateDao().getPetState()?.timeTokens)
            assertNull(database.taskCompletionDao().get(DilemmaCatalog.ROBOT_ID))
            assertIs<TaskSubmission.Completed>(TasksRepository(database).complete(DilemmaCatalog.ROBOT_ID, "wait").getOrNull())
        } finally {
            database.close()
        }
    }

    @Test
    fun failedCompletionWriteRollsBackMoneyAndTime() = runBlocking<Unit> {
        val path = Files.createTempDirectory("finni-rollback-test").resolve("game.db").toString()
        var database = openDatabase(path)
        try {
            val account = AccountEntity()
            val pet = PetStateEntity()
            database.accountDao().upsertAccountState(account)
            database.petStateDao().upsertPetState(pet)
            database.close()
            BundledSQLiteDriver().open(path).use { connection ->
                connection.prepare("CREATE TRIGGER reject_completion BEFORE INSERT ON task_completion BEGIN SELECT RAISE(ABORT, 'test failure'); END").use { it.step() }
            }
            database = openDatabase(path)
            assertTrue(TasksRepository(database).complete(DilemmaCatalog.ROBOT_ID, "repair").isLeft())
            assertEquals(account, database.accountDao().getAccountState())
            assertEquals(pet, database.petStateDao().getPetState())
            assertNull(database.taskCompletionDao().get(DilemmaCatalog.ROBOT_ID))
        } finally {
            database.close()
        }
    }

    @Test
    fun concurrentSubmissionsAndReopeningDoNotRepeatReward() = runBlocking<Unit> {
        val path = Files.createTempDirectory("finni-task-test").resolve("game.db").toString()
        var database = openDatabase(path)
        try {
            database.accountDao().upsertAccountState(AccountEntity(walletCoins = 70, savingsCoins = 50, savingsStreakDays = 2))
            database.petStateDao().upsertPetState(PetStateEntity(timeTokens = 1, timePhase = "Evening"))
            val repository = TasksRepository(database)
            val results = coroutineScope {
                List(8) { async(Dispatchers.Default) { repository.complete(DilemmaCatalog.ROBOT_ID, "repair").getOrNull() } }.awaitAll()
            }
            results.forEach { assertIs<TaskSubmission.Completed>(it) }
            assertEquals(1, results.filterIsInstance<TaskSubmission.Completed>().count { it.isNew })
            assertEquals(90, database.accountDao().getAccountState()?.walletCoins)
            assertEquals(30, database.accountDao().getAccountState()?.savingsCoins)
            assertEquals(0, database.accountDao().getAccountState()?.savingsStreakDays)
            assertEquals(0, database.petStateDao().getPetState()?.timeTokens)
            assertEquals("Night", database.petStateDao().getPetState()?.timePhase)

            database.close()
            database = openDatabase(path)
            val replay = assertIs<TaskSubmission.Completed>(TasksRepository(database).complete(DilemmaCatalog.ROBOT_ID, "wait").getOrNull())
            assertEquals("repair", replay.completion.choiceId)
            assertTrue(!replay.isNew)
            assertEquals(90, database.accountDao().getAccountState()?.walletCoins)
            assertEquals(30, database.accountDao().getAccountState()?.savingsCoins)
            assertEquals(0, database.petStateDao().getPetState()?.timeTokens)
        } finally {
            database.close()
        }
    }

    @Test
    fun rejectedRepairDoesNotChangeStateAndFreeChoiceStillWorks() = runBlocking<Unit> {
        val path = Files.createTempDirectory("finni-task-test").resolve("game.db").toString()
        val database = openDatabase(path)
        try {
            val account = AccountEntity(walletCoins = 0, savingsCoins = 19)
            val pet = PetStateEntity()
            database.accountDao().upsertAccountState(account)
            database.petStateDao().upsertPetState(pet)
            val repository = TasksRepository(database)
            assertEquals(TaskSubmission.Blocked(TaskBlockReason.INSUFFICIENT_SAVINGS), repository.complete(DilemmaCatalog.ROBOT_ID, "repair").getOrNull())
            assertEquals(account, database.accountDao().getAccountState())
            assertEquals(pet, database.petStateDao().getPetState())
            assertNull(database.taskCompletionDao().get(DilemmaCatalog.ROBOT_ID))

            assertIs<TaskSubmission.Completed>(repository.complete(DilemmaCatalog.ROBOT_ID, "wait").getOrNull())
            assertEquals(20, database.accountDao().getAccountState()?.walletCoins)
            assertEquals(19, database.accountDao().getAccountState()?.savingsCoins)
            assertEquals(2, database.petStateDao().getPetState()?.timeTokens)
            assertNotNull(database.taskCompletionDao().get(DilemmaCatalog.ROBOT_ID))
        } finally {
            database.close()
        }
    }
}
