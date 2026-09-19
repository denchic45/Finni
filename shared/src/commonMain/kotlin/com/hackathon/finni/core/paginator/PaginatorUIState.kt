package com.hackathon.finni.core.paginator

sealed interface PageState {
    data object None : PageState
    data object Loading : PageState
    data class Error(val exception: Exception) : PageState
}

sealed interface PaginatorUIState<out T> {
    data object Loading : PaginatorUIState<Nothing>
    data class Error(val exception: Exception) : PaginatorUIState<Nothing>
    data class Content<T>(
        val items: List<T>,
        val appendState: PageState = PageState.None,
        val prependState: PageState = PageState.None,
        val error: Exception? = null
    ) : PaginatorUIState<T>
}
