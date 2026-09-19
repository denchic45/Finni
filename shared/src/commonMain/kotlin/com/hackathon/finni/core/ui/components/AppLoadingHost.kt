package com.hackathon.finni.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.presentation.handlers.LoadingHandler
import com.hackathon.finni.core.presentation.handlers.LoadingState
import com.hackathon.finni.core.ui.extension.getStringResource
import org.koin.compose.koinInject

/**
 * Главный UI-хост для отображения загрузки. 
 * Автоматически подписывается на [LoadingHandler] и рендерит нужный тип индикатора.
 */
@Composable
fun LoadingHost(
    loadingHandler: LoadingHandler = koinInject(),
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = { loadingHandler.hideLoading() }
) {
    val loadingState by loadingHandler.loading.collectAsStateWithLifecycle()

    LoadingHost(
        loadingState = loadingState,
        onCancel = onCancel,
        modifier = modifier
    )
}

/**
 * Перегрузка для визуализации состояния в Compose Previews или независимого вызова.
 */
@Composable
fun LoadingHost(
    loadingState: LoadingState?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (loadingState) {
        is LoadingState.Overlay -> FullScreenOverlayLoading(modifier = modifier)
        is LoadingState.Dialog -> LoadingDialog(state = loadingState, onCancel = onCancel)
        is LoadingState.Snackbar -> LoadingSnackbar(state = loadingState, onCancel = onCancel, modifier = modifier)
        null -> Unit
    }
}

// ============================================================================
// Realizations
// ============================================================================

/**
 * 1. Мгновенный полупрозрачный оверлей с блокировкой кликов
 */
@Composable
private fun FullScreenOverlayLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .pointerInput(Unit) {}, // Перехватывает клики, чтобы пользователь не тапал сквозь оверлей
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 2. Модальный диалог с заголовком, подзаголовком и поддержкой Determinate/Indeterminate прогресса
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingDialog(
    state: LoadingState.Dialog,
    onCancel: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {
            if (state.cancelActionLabel != null) onCancel()
        },
        properties = DialogProperties(
            dismissOnBackPress = state.cancelActionLabel != null,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Спиннер с детерминированным или неопределенным прогрессом
                if (state.progress != null) {
                    CircularProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок загрузки
                Text(
                    text = state.message.getStringResource(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Описание (если есть)
                state.subMessage?.let { sub ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sub.getStringResource(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Кнопка отмены
                if (state.cancelActionLabel != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancel) {
                            Text(text = state.cancelActionLabel.getStringResource())
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Неблокирующая снизу плашка (Snackbar)
 */
@Composable
private fun LoadingSnackbar(
    state: LoadingState.Snackbar,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Snackbar(
            action = state.cancelActionLabel?.let { label ->
                {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = label.getStringResource(),
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                }
            },
            dismissAction = null,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.progress != null) {
                    CircularProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }

                Text(
                    text = state.message.getStringResource(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}