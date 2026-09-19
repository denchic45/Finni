package com.hackathon.finni.core.paginator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update


@OptIn(ExperimentalCoroutinesApi::class)
class Paginator<T>(
    private val pageSize: Int = 20,
    private val observeLocalPages: (activePages: Set<Int>) -> Flow<Map<Int, List<T>>>,
    private val fetchPage: suspend (page: Int) -> Int,
    private val defaultStrategy: FetchStrategy = FetchStrategy.NetworkFirstWithFallback
) {
    private val activePages = MutableStateFlow<Set<Int>>(emptySet())
    private val appendState = MutableStateFlow<PageState>(PageState.None)
    private val prependState = MutableStateFlow<PageState>(PageState.None)
    private val error = MutableStateFlow<Exception?>(null)

    val isEndReached: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isStartReached: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val loadedPages: StateFlow<Map<Int, List<T>>>
        field = MutableStateFlow<Map<Int, List<T>>>(emptyMap())

    val uiState: Flow<PaginatorUIState<T>> = activePages.flatMapLatest { pages ->
        if (pages.isEmpty()) {
            // ⏳ 1. Пока страницы не заданы — комбинируем только статус ошибки
            combine(appendState, prependState, error) { _, _, err ->
                if (err != null) {
                    PaginatorUIState.Error(err)
                } else {
                    PaginatorUIState.Loading
                }
            }
        } else {
            // 📦 2. Страницы заданы — подписываемся на Room.
            combine(
                observeLocalPages(pages),
                appendState,
                prependState,
                error
            ) { items, append, prepend, err ->
                loadedPages.value = items
                PaginatorUIState.Content(
                    items = items.values.flatten(),
                    appendState = append,
                    prependState = prepend,
                    error = err
                )
            }
        }
    }.distinctUntilChanged()

    private var lastRestartPage: Int = 1
    suspend fun restart(page: Int = lastRestartPage, strategy: FetchStrategy = defaultStrategy) {
        lastRestartPage = page
        clearAllPages()

        loadPage(page, strategy)
    }

    fun clearAllPages() {
        activePages.value = emptySet()
        loadedPages.value = emptyMap()
        error.value = null
        appendState.value = PageState.None
        prependState.value = PageState.None
        isEndReached.value = false
        isStartReached.value = false
    }

    suspend fun refresh(
        strategy: FetchStrategy = defaultStrategy,
        asyncLoadPages: Boolean = true
    ) {
        val currentPages = activePages.value.sorted()

        if (currentPages.isEmpty()) {
            restart(strategy = strategy)
            return
        }

        error.value = null

        try {
            when (asyncLoadPages) {
                true -> coroutineScope {
                    currentPages.map { page ->
                        async { fetchPage(page) }
                    }.awaitAll()
                }

                false -> for (page in currentPages) {
                    fetchPage(page)
                }
            }
        } catch (e: Exception) {
            error.value = e
        }
    }

    suspend fun loadPage(
        page: Int,
        strategy: FetchStrategy = defaultStrategy
    ) {
        if (appendState.value is PageState.Loading) return

        when (strategy) {
            FetchStrategy.CacheOnly -> {
                activePages.update { it + page }
            }

            FetchStrategy.StaleWhileRevalidate -> {
                activePages.update { it + page }
                appendState.value = PageState.Loading
                try {
                    fetchPage(page)
                    appendState.value = PageState.None
                } catch (e: Exception) {
                    appendState.value = PageState.Error(e)
                    error.value = e
                }
            }

            FetchStrategy.NetworkFirstGuarded -> {
                appendState.value = PageState.Loading
                try {
                    fetchPage(page)
                    appendState.value = PageState.None
                    activePages.update { it + page }
                } catch (e: Exception) {
                    appendState.value = PageState.Error(e)
                    error.value = e
                }
            }

            FetchStrategy.NetworkFirstWithFallback -> {
                appendState.value = PageState.Loading
                try {
                    fetchPage(page)
                    appendState.value = PageState.None
                } catch (e: Exception) {
                    appendState.value = PageState.Error(e)
                    error.value = e
                } finally {
                    activePages.update { it + page }
                }
            }
        }
    }

    fun getGlobalIndex(localIndex: Int): Int {
        val minPage = activePages.value.minOrNull() ?: return -1
        val startOffset = (minPage - 1) * pageSize
        return startOffset + localIndex
    }

    fun getLocalIndex(globalIndex: Int): Int {
        val minPage = activePages.value.minOrNull() ?: return -1
        val startOffset = (minPage - 1) * pageSize
        val localIndex = globalIndex - startOffset
        val currentCount = loadedPages.value.size

        return if (localIndex in 0 until currentCount) {
            localIndex
        } else {
            -1
        }
    }

    fun getPageByGlobalIndex(globalIndex: Int): Int {
        if (globalIndex < 0) return -1
        return (globalIndex / pageSize) + 1
    }

    fun getPageByLocalIndex(localIndex: Int): Int {
        val globalIndex = getGlobalIndex(localIndex)
        return getPageByGlobalIndex(globalIndex)
    }

    suspend fun loadNext() {
        if (appendState.value is PageState.Loading
            || isEndReached.value
            || loadedPages.value.isEmpty()
            || activePages.value.isEmpty()
        ) return

        val nextPage = (activePages.value.maxOrNull() ?: 0) + 1
        appendState.value = PageState.Loading
        error.value = null

        try {
            val fetchedItemsCount = fetchPage(nextPage)
            isEndReached.value = fetchedItemsCount == 0

            appendState.value = PageState.None
            activePages.update { it + nextPage }

        } catch (e: Exception) {
            appendState.value = PageState.Error(e)
            error.value = e
        }
    }

    suspend fun loadPrevious() {
        if (prependState.value is PageState.Loading
            || isStartReached.value
            || loadedPages.value.isEmpty()
            || activePages.value.isEmpty()
        ) return

        val prevPage = (activePages.value.minOrNull() ?: 1) - 1
        if (prevPage < 1) {
            isStartReached.value = true
            return
        }
        prependState.value = PageState.Loading
        error.value = null

        try {
            val fetchedItemsCount = fetchPage(prevPage)
            isStartReached.value = fetchedItemsCount == 0

            prependState.value = PageState.None
            activePages.update { it + prevPage }
        } catch (e: Exception) {
            prependState.value = PageState.Error(e)
            error.value = e
        }
    }

    suspend fun retry() {
        if (error.value == null) return
        if (appendState.value is PageState.Error) {
            loadNext()
        }
        if (prependState.value is PageState.Error) {
            loadPrevious()
        }
        if (activePages.value.isEmpty()) {
            restart()
        }
    }
}
