package com.hackathon.finni.features.tasks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DilemmaRulesTest {
    private val repair = DilemmaEffect(20, -20, "Neutral", false)
    private val wait = DilemmaEffect(20, 0, "Happy", true)

    @Test
    fun rewardDoesNotPayForMissingSavings() {
        val result = assertIs<DilemmaEvaluation.Blocked>(evaluateDilemma(repair, 19, 4, 3))
        assertEquals(TaskBlockReason.INSUFFICIENT_SAVINGS, result.reason)
    }

    @Test
    fun freeChoiceIsAvailableWithEmptyAccounts() {
        val result = assertIs<DilemmaEvaluation.Accepted>(evaluateDilemma(wait, 0, 1, 3))
        assertEquals(0, result.effect.savingsDelta)
        assertEquals(2, result.tokensLeft)
        assertEquals("Day", result.phase)
    }

    @Test
    fun finalTokenFinishesTheDay() {
        val result = assertIs<DilemmaEvaluation.Accepted>(evaluateDilemma(repair, 20, 1, 1))
        assertEquals(0, result.tokensLeft)
        assertEquals("Night", result.phase)
    }

    @Test
    fun unavailableTasksDoNotProduceEffects() {
        assertEquals(DilemmaEvaluation.Blocked(TaskBlockReason.NO_TIME), evaluateDilemma(wait, 50, 4, 0))
        assertEquals(DilemmaEvaluation.Blocked(TaskBlockReason.HUNGRY), evaluateDilemma(wait, 50, 0, 3))
        assertEquals(DilemmaEvaluation.Blocked(TaskBlockReason.UNKNOWN_CHOICE), evaluateDilemma(null, 50, 4, 3))
    }

    @Test
    fun catalogContainsEighteenUniqueTasksWithPlayableChoices() {
        assertEquals(18, DilemmaCatalog.all.size)
        assertEquals(18, DilemmaCatalog.all.map { it.id }.toSet().size)
        DilemmaCatalog.all.forEach { task ->
            assertEquals(task, DilemmaCatalog.byId(task.id))
            kotlin.test.assertTrue(task.choices.size >= 2)
            kotlin.test.assertTrue(task.choices.map { it.id }.toSet().size == task.choices.size)
        }
    }

    @Test
    fun everyLevelHasAPlayableTask() {
        (1..18).forEach { levelId ->
            val taskId = kotlin.test.assertNotNull(LevelTaskCatalog.taskIdFor(levelId))
            kotlin.test.assertNotNull(DilemmaCatalog.byId(taskId))
        }
    }
}
