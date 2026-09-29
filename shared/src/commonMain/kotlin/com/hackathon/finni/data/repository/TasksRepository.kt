package com.hackathon.finni.data.repository

import arrow.core.left
import arrow.core.right
import com.hackathon.finni.core.presentation.error.asFailure
import com.hackathon.finni.data.RequestResult
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.entity.TaskCompletionEntity
import com.hackathon.finni.data.database.withTransaction
import com.hackathon.finni.features.tasks.DilemmaCatalog
import com.hackathon.finni.features.tasks.DilemmaEvaluation
import com.hackathon.finni.features.tasks.TaskBlockReason
import com.hackathon.finni.features.tasks.evaluateDilemma
import kotlinx.coroutines.CancellationException

sealed interface TaskSubmission {
    data class Completed(
        val completion: TaskCompletionEntity,
        val isNew: Boolean
    ) : TaskSubmission
    data class Blocked(val reason: TaskBlockReason) : TaskSubmission
}

class TasksRepository(private val database: AppDatabase) {
    fun completion(taskId: String) = database.taskCompletionDao().observe(taskId)

    suspend fun complete(taskId: String, choiceId: String): RequestResult<TaskSubmission> = try {
        database.withTransaction {
            val task = DilemmaCatalog.byId(taskId)
                ?: return@withTransaction TaskSubmission.Blocked(TaskBlockReason.NOT_READY)
            val completionDao = database.taskCompletionDao()
            val existing = completionDao.get(task.id)
            if (existing != null) return@withTransaction TaskSubmission.Completed(existing, isNew = false)

            val pet = database.petStateDao().getPetState()
                ?: return@withTransaction TaskSubmission.Blocked(TaskBlockReason.NOT_READY)
            val account = database.accountDao().getAccountState()
                ?: return@withTransaction TaskSubmission.Blocked(TaskBlockReason.NOT_READY)
            val choice = task.choices.find { it.id == choiceId }
            when (val evaluation = evaluateDilemma(choice?.effect, account.savingsCoins, pet.hunger, pet.timeTokens)) {
                is DilemmaEvaluation.Blocked -> TaskSubmission.Blocked(evaluation.reason)
                is DilemmaEvaluation.Accepted -> {
                    val effect = evaluation.effect
                    database.accountDao().upsertAccountState(account.copy(
                        walletCoins = account.walletCoins + effect.walletDelta,
                        savingsCoins = account.savingsCoins + effect.savingsDelta,
                        savingsStreakDays = if (effect.savingsDelta < 0) 0 else account.savingsStreakDays
                    ))
                    database.petStateDao().upsertPetState(pet.copy(
                        mood = effect.mood,
                        timeTokens = evaluation.tokensLeft,
                        timePhase = evaluation.phase
                    ))
                    val completion = TaskCompletionEntity(
                        task.id, choiceId, effect.walletDelta, effect.savingsDelta, effect.mood, effect.isOptimal
                    )
                    completionDao.insert(completion)
                    TaskSubmission.Completed(completion, isNew = true)
                }
            }
        }.right()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: Exception) {
        exception.asFailure().left()
    }
}
