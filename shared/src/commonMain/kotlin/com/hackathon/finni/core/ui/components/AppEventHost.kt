package com.hackathon.finni.core.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.presentation.handlers.EventHandler
import com.hackathon.finni.core.presentation.handlers.UIEvent
import com.hackathon.finni.core.ui.extension.getString
import com.hackathon.finni.core.ui.extension.getStringResource
import com.hackathon.finni.core.ui.extension.rememberPainter
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Главный UI-хост для обработки разовых событий [UIEvent] из [EventHandler].
 * 
 * Размещается в корневом контейнере экрана или приложения (поверх Scaffold).
 */
@Composable
fun AppEventHandlerHost(
    eventHandler: EventHandler = koinInject(),
    toastManager: ToastManager = koinInject(),
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scope = rememberCoroutineScope()

    // Состояние активного модального диалога
    var currentAlert by remember { mutableStateOf<UIEvent.AlertMessage?>(null) }

    // Сбор разовых событий из Flow
    LaunchedEffect(eventHandler) {
        eventHandler.events.collect { event ->
            when (event) {
                is UIEvent.Toast -> {
                    toastManager.showToast(
                        message = event.message.getString(),
                        toastDuration = event.duration
                    )
                }

                is UIEvent.Snackbar -> {
                    val message = event.message.getString()
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = event.action?.label?.getString(),
                            duration = SnackbarDuration.Short
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.onClick?.invoke()
                        }
                    }
                }

                is UIEvent.AlertMessage -> {
                    currentAlert = event
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Хост для снакбаров (снизу экрана)
//        SnackbarHost(
//            hostState = snackbarHostState,
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                // На мобильном размещаем снизу с отступом под тулбар,
                // на десктопе — в правом нижнем углу
                .align(Alignment.BottomCenter)
                // Отступ снизу, чтобы не перекрывать Floating Bar (например, 88.dp)
                .padding(
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .navigationBarsPadding() // Учитываем системную панель жестов
                .imePadding()            // Поднимаем над клавиатурой, если она открыта
                .widthIn(max = 420.dp),  // Ограничение ширины для планшетов и десктопа
            snackbar = { data ->
                // Опционально: стилизация самого снейкбара под круглый стиль интерфейса
                SwipeToDismissBox(
                    state = rememberSwipeToDismissBoxState(),
                    backgroundContent = {}
                ) {
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        )

        // Отображение диалога (если есть активный AlertMessage)
        currentAlert?.let { alert ->
            EventAlertDialog(
                alert = alert,
                onDismiss = { currentAlert = null }
            )
        }
    }
}

/**
 * Диалог на базе Material 3 для отображения UIEvent.AlertMessage
 */
@Composable
private fun EventAlertDialog(
    alert: UIEvent.AlertMessage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            alert.dismissAction?.onClick?.invoke()
            onDismiss()
        },
        title = {
            Text(
                text = alert.title.getStringResource(),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = alert.text?.let { text ->
            {
                Text(
                    text = text.getStringResource(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        icon = alert.icon?.let { icon ->
            {
                Icon(
                    painter = icon.rememberPainter(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    alert.confirmAction.onClick()
                    onDismiss()
                }
            ) {
                Text(text = alert.confirmAction.label.getStringResource())
            }
        },
        dismissButton = alert.dismissAction?.let { dismiss ->
            {
                TextButton(
                    onClick = {
                        dismiss.onClick()
                        onDismiss()
                    }
                ) {
                    Text(text = dismiss.label.getStringResource())
                }
            }
        }
    )
}