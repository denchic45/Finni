package com.hackathon.finni.core.ui.components.resource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.resource.CacheableResource
import com.hackathon.finni.core.resource.getValueOrNull

@Composable
fun <T> CacheableResourceContent(
    resource: CacheableResource<T>,
    content: @Composable (T) -> Unit
) {
    CacheableResourceContent(
        resource = resource,
        dataContent = content
    )
}

@Composable
fun <T> CacheableResourceContent(
    resource: CacheableResource<T>,
    onRetry: (() -> Unit)? = null,
    retrying: Boolean = false,
    loadingContent: @Composable () -> Unit = {
        CircularLoadingBox(
            Modifier.fillMaxSize()
        )
    },
    dataContent: @Composable (T) -> Unit,
    failedContent: @Composable (UiError) -> Unit = { error ->
        DefaultFailedContent(
            error = error,
            isRetrying = retrying,
            onRetry = onRetry
        )
    },
    cachedErrorContent: @Composable BoxScope.(UiError?) -> Unit = { error ->
        DefaultCachedErrorSnackbar(
            error = error,
            onRetry = onRetry,
            retrying = retrying
        )
    }
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (resource) {
            CacheableResource.Loading -> loadingContent()
            is CacheableResource.Newest -> dataContent(resource.value)
            is CacheableResource.Cached -> dataContent(resource.value)
            is CacheableResource.Failed -> failedContent(resource.error)
        }

        cachedErrorContent((resource as? CacheableResource.Cached)?.error)
    }
}

@Composable
fun <T> CacheableResourceListContent(
    resource: CacheableResource<List<T>>,
    onRetry: (() -> Unit)? = null,
    retrying: Boolean = false,
    loadingContent: @Composable () -> Unit = {
        CircularLoadingBox(Modifier.fillMaxSize())
    },
    dataContent: @Composable (List<T>) -> Unit,
    failedContent: @Composable (UiError) -> Unit = { error ->
        DefaultFailedContent(
            error = error,
            isRetrying = retrying,
            onRetry = onRetry
        )
    },
    cachedErrorContent: @Composable BoxScope.(UiError?) -> Unit = { error ->
        DefaultCachedErrorSnackbar(
            error = error,
            onRetry = onRetry,
            retrying = retrying
        )
    },
    emptyDataContent: @Composable () -> Unit = {}
) {
    CacheableResourceContent(
        resource = resource,
        onRetry = onRetry,
        retrying = retrying,
        loadingContent = loadingContent,
        dataContent = { items ->
            // TODO уточнить, нет ли вреда для производительности из-за переключения с dataContent на emptyDataContent
            if (items.isNotEmpty()) dataContent(items)
            else emptyDataContent()
        },
        failedContent = failedContent,
        cachedErrorContent = { error ->
            val items = resource.getValueOrNull() ?: emptyList()
            if (items.isNotEmpty()) {
                cachedErrorContent(error)
            } else if (error != null) {
                failedContent(error)
            }
        }
    )
}
