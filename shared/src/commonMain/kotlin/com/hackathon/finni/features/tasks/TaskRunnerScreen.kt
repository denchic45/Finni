package com.hackathon.finni.features.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.presentation.handlers.BackHandler
import com.hackathon.finni.core.ui.extension.getStringResource
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.task_action_back
import com.hackathon.finni.resources.task_action_confirm
import com.hackathon.finni.resources.task_action_cancel
import com.hackathon.finni.resources.task_balances
import com.hackathon.finni.resources.task_effect_summary
import com.hackathon.finni.resources.task_confirmation_title
import com.hackathon.finni.resources.task_withdrawal_warning
import com.hackathon.finni.resources.task_result_title
import com.hackathon.finni.resources.task_result_saved
import com.hackathon.finni.resources.task_mood_happy
import com.hackathon.finni.resources.task_mood_neutral
import com.hackathon.finni.resources.task_block_no_time
import com.hackathon.finni.resources.task_block_hungry
import com.hackathon.finni.resources.task_block_savings
import com.hackathon.finni.resources.task_block_not_ready
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaskRunnerScreen(
    taskId: String,
    levelId: Int? = null,
    viewModel: TaskRunnerViewModel = koinViewModel()
) {
    LaunchedEffect(taskId, levelId) { viewModel.onTaskOpened(taskId, levelId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = state.isSaving) { }
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = viewModel::onBack, enabled = !state.isSaving) {
                Text(stringResource(Res.string.task_action_back))
            }
            Text(viewModel.task.title.getStringResource(), style = MaterialTheme.typography.headlineSmall)
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(Res.string.task_balances, state.wallet, state.savings, state.tokens))
                val completion = state.completion
                if (completion != null) {
                    Text(stringResource(Res.string.task_result_title), style = MaterialTheme.typography.titleLarge)
                    viewModel.task.choices.find { it.id == completion.choiceId }?.let { choice ->
                        Text(choice.feedback.getStringResource())
                    }
                    EffectSummary(completion.walletDelta, completion.savingsDelta, completion.mood)
                    Text(stringResource(Res.string.task_result_saved))
                    Button(onClick = viewModel::onBack, enabled = !state.isSaving) {
                        Text(stringResource(Res.string.task_action_back))
                    }
                } else {
                    DilemmaRenderer(viewModel.task, !state.isSaving, viewModel::onChoiceSelected)
                    state.blocked?.let { reason ->
                        Text(stringResource(when (reason) {
                            TaskBlockReason.NO_TIME -> Res.string.task_block_no_time
                            TaskBlockReason.HUNGRY -> Res.string.task_block_hungry
                            TaskBlockReason.INSUFFICIENT_SAVINGS -> Res.string.task_block_savings
                            else -> Res.string.task_block_not_ready
                        }))
                    }
                }
            }
        }
    }
    val choice = state.selectedChoice
    if (choice != null && state.completion == null) {
        AlertDialog(
            onDismissRequest = viewModel::onConfirmationDismissed,
            title = { Text(stringResource(Res.string.task_confirmation_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(choice.title.getStringResource())
                    if (choice.effect.savingsDelta < 0) Text(stringResource(Res.string.task_withdrawal_warning))
                    EffectSummary(choice.effect.walletDelta, choice.effect.savingsDelta, choice.effect.mood)
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmChoice, enabled = !state.isSaving) {
                    Text(stringResource(Res.string.task_action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onConfirmationDismissed, enabled = !state.isSaving) {
                    Text(stringResource(Res.string.task_action_cancel))
                }
            }
        )
    }
    if (state.tetrisUnlockedNow) {
        AlertDialog(
            onDismissRequest = viewModel::onTetrisUnlockMessageDismissed,
            title = { Text("ТЕТРИС ОТКРЫТ!") },
            text = { Text("Ты прошёл 10-й уровень. Теперь мини-игра доступна по фиолетовой иконке на главном экране.") },
            confirmButton = {
                TextButton(onClick = viewModel::onTetrisUnlockMessageDismissed) { Text("ЗДОРОВО") }
            }
        )
    }
}

@Composable
fun DilemmaRenderer(task: DilemmaTask, enabled: Boolean, onChoiceSelected: (String) -> Unit) {
    Column(Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(task.story.getStringResource(), style = MaterialTheme.typography.bodyLarge)
        task.choices.forEach { choice ->
            OutlinedButton(
                onClick = { onChoiceSelected(choice.id) },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(choice.title.getStringResource(), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun EffectSummary(walletDelta: Int, savingsDelta: Int, mood: String) {
    val moodText = stringResource(if (mood == "Happy") Res.string.task_mood_happy else Res.string.task_mood_neutral)
    Text(stringResource(Res.string.task_effect_summary, walletDelta, savingsDelta, moodText))
}
