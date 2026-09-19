package com.hackathon.finni.core.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.presentation.handlers.RefreshHandler

/**
 * Главный оберточный контейнер для экранов с поддержкой PullToRefresh.
 * Принимает [RefreshHandler] и автоматически подписывается на его состояние.
 */
@Composable
fun AppPullToRefreshBox(
    refreshHandler: RefreshHandler,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isRefreshing by refreshHandler.isRefreshing.collectAsStateWithLifecycle()

    AppPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = refreshHandler::onRefresh,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * Базовая перегрузка для использования в Превью или без делегата.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    if (enabled) {
        val state = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = state,
            modifier = modifier,
            content = content
        )
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}