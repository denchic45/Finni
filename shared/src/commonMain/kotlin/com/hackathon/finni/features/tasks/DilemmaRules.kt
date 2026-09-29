package com.hackathon.finni.features.tasks

data class DilemmaEffect(
    val walletDelta: Int,
    val savingsDelta: Int,
    val mood: String,
    val isOptimal: Boolean
)

enum class TaskBlockReason { NOT_READY, NO_TIME, HUNGRY, INSUFFICIENT_SAVINGS, UNKNOWN_CHOICE }

sealed interface DilemmaEvaluation {
    data class Accepted(val effect: DilemmaEffect, val tokensLeft: Int, val phase: String) : DilemmaEvaluation
    data class Blocked(val reason: TaskBlockReason) : DilemmaEvaluation
}

/** Pure rules: the reward cannot finance a purchase from the savings account. */
fun evaluateDilemma(
    effect: DilemmaEffect?,
    savings: Int,
    hunger: Int,
    tokens: Int
): DilemmaEvaluation {
    val reason = when {
        effect == null -> TaskBlockReason.UNKNOWN_CHOICE
        tokens <= 0 -> TaskBlockReason.NO_TIME
        hunger <= 0 -> TaskBlockReason.HUNGRY
        savings + effect.savingsDelta < 0 -> TaskBlockReason.INSUFFICIENT_SAVINGS
        else -> null
    }
    if (reason != null) return DilemmaEvaluation.Blocked(reason)
    val remaining = (tokens - 1).coerceIn(0, 3)
    return DilemmaEvaluation.Accepted(
        effect = requireNotNull(effect),
        tokensLeft = remaining,
        phase = when (remaining) {
            2 -> "Day"
            1 -> "Evening"
            0 -> "Night"
            else -> "Morning"
        }
    )
}
