package com.hackathon.finni.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.paginator.PageState
import com.hackathon.finni.core.paginator.shouldShowAppend
import com.hackathon.finni.core.paginator.shouldShowPrepend

/**
 * Стандартный заголовок секции для списков и сеток.
 */
@Composable
fun AppSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

/**
 * Компонент для отображения статуса загрузки или ошибки страницы (append/prepend).
 */
@Composable
fun PageStatusItem(
    state: PageState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is PageState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            is PageState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ошибка загрузки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetry) {
                        Text("Повторить")
                    }
                }
            }
            else -> {}
        }
    }
}

// --- LazyListScope Extensions ---

fun LazyListScope.prependItem(
    state: LazyListState,
    prependState: PageState,
    onRetry: () -> Unit,
    content: @Composable (PageState) -> Unit = { PageStatusItem(it, onRetry) }
) {
    item(key = "paginator_prepend") {
        if (state.shouldShowPrepend(prependState)) {
            content(prependState)
        }
    }
}

fun LazyListScope.appendItem(
    state: LazyListState,
    appendState: PageState,
    onRetry: () -> Unit,
    content: @Composable (PageState) -> Unit = { PageStatusItem(it, onRetry) }
) {
    item(key = "paginator_append") {
        if (state.shouldShowAppend(appendState)) {
            content(appendState)
        }
    }
}

// --- LazyGridScope Extensions ---

fun LazyGridScope.prependItem(
    state: LazyGridState,
    prependState: PageState,
    onRetry: () -> Unit,
    content: @Composable LazyGridItemScope.(PageState) -> Unit = { PageStatusItem(it, onRetry) }
) {
    item(key = "paginator_prepend", span = { GridItemSpan(maxLineSpan) }) {
        if (state.shouldShowPrepend(prependState)) {
            content(prependState)
        }
    }
}

fun LazyGridScope.appendItem(
    state: LazyGridState,
    appendState: PageState,
    onRetry: () -> Unit,
    content: @Composable LazyGridItemScope.(PageState) -> Unit = { PageStatusItem(it, onRetry) }
) {
    item(key = "paginator_append", span = { GridItemSpan(maxLineSpan) }) {
        if (state.shouldShowAppend(appendState)) {
            content(appendState)
        }
    }
}

