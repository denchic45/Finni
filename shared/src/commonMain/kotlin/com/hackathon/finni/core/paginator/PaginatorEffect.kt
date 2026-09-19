package com.hackathon.finni.core.paginator

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Эффект для автоматической подгрузки страниц при скролле.
 *
 * @param state Состояние списка (LazyListState).
 * @param paginator Экземпляр пагинатора.
 * @param threshold Количество элементов до конца/начала списка, при котором срабатывает подгрузка.
 */
@Composable
fun <T> PaginatorEffect(
    state: LazyListState,
    paginator: Paginator<T>,
    threshold: Int = 5
) {
    val loadNext by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            if (totalItemsCount > 0) {
                totalItemsCount - lastVisibleItemIndex <= threshold
            } else {
                false
            }
        }
    }

    val loadPrevious by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

            if (layoutInfo.totalItemsCount > 0) {
                firstVisibleItemIndex <= threshold
            } else {
                false
            }
        }
    }

    LaunchedEffect(loadNext) {
        if (loadNext) {
            paginator.loadNext()
        }
    }

    LaunchedEffect(loadPrevious) {
        if (loadPrevious) {
            paginator.loadPrevious()
        }
    }
}

/**
 * Эффект для автоматической подгрузки страниц при скролле для сеток.
 *
 * @param state Состояние сетки (LazyGridState).
 * @param paginator Экземпляр пагинатора.
 * @param threshold Количество элементов до конца/начала списка, при котором срабатывает подгрузка.
 */
@Composable
fun <T> PaginatorEffect(
    state: LazyGridState,
    paginator: Paginator<T>,
    threshold: Int = 5
) {
    val loadNext by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            if (totalItemsCount > 0) {
                totalItemsCount - lastVisibleItemIndex <= threshold
            } else {
                false
            }
        }
    }

    val loadPrevious by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

            if (layoutInfo.totalItemsCount > 0) {
                firstVisibleItemIndex <= threshold
            } else {
                false
            }
        }
    }

    LaunchedEffect(loadNext) {
        if (loadNext) {
            paginator.loadNext()
        }
    }

    LaunchedEffect(loadPrevious) {
        if (loadPrevious) {
            paginator.loadPrevious()
        }
    }
}

/**
 * Проверяет, нужно ли отображать индикатор загрузки в конце списка.
 * Возвращает false, если все элементы списка помещаются на одном экране.
 */
@Composable
fun LazyListState.shouldShowAppend(appendState: PageState): Boolean {
    val isScrollable by remember {
        derivedStateOf { canScrollForward || canScrollBackward }
    }
    return (appendState is PageState.Loading || appendState is PageState.Error) && isScrollable
}

/**
 * Проверяет, нужно ли отображать индикатор загрузки в начале списка.
 * Возвращает false, если все элементы списка помещаются на одном экране.
 */
@Composable
fun LazyListState.shouldShowPrepend(prependState: PageState): Boolean {
    val isScrollable by remember {
        derivedStateOf { canScrollForward || canScrollBackward }
    }
    return (prependState is PageState.Loading || prependState is PageState.Error) && isScrollable
}

/**
 * Проверяет, нужно ли отображать индикатор загрузки в конце сетки.
 * Возвращает false, если все элементы сетки помещаются на одном экране.
 */
@Composable
fun LazyGridState.shouldShowAppend(appendState: PageState): Boolean {
    val isScrollable by remember {
        derivedStateOf { canScrollForward || canScrollBackward }
    }
    return (appendState is PageState.Loading || appendState is PageState.Error) && isScrollable
}

/**
 * Проверяет, нужно ли отображать индикатор загрузки в начале сетки.
 * Возвращает false, если все элементы сетки помещаются на одном экране.
 */
@Composable
fun LazyGridState.shouldShowPrepend(prependState: PageState): Boolean {
    val isScrollable by remember {
        derivedStateOf { canScrollForward || canScrollBackward }
    }
    return (prependState is PageState.Loading || prependState is PageState.Error) && isScrollable
}

