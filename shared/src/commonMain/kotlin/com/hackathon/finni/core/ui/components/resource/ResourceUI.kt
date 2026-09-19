package com.hackathon.finni.core.ui.components.resource

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.resource.Resource

@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    onLoading: @Composable () -> Unit = { CircularLoadingBox(Modifier.fillMaxSize()) },
    onFailed: @Composable (UiError) -> Unit = { failure -> DefaultFailedContent(failure) },
    onSuccess: @Composable (T) -> Unit,
) {
    when (resource) {
        Resource.Loading -> onLoading()
        is Resource.Success -> onSuccess(resource.value)
        is Resource.Failed -> onFailed(resource.error)
    }
}

@Composable
fun <T> CrossfadeResourceContent(
    resource: Resource<T>,
    onLoading: @Composable () -> Unit = { CircularLoadingBox(Modifier.fillMaxSize()) },
    onFailed: @Composable (UiError) -> Unit = { failure -> DefaultFailedContent(failure) },
    onSuccess: @Composable (T) -> Unit,
) {
    Crossfade(targetState = resource) { state ->
        when (state) {
            Resource.Loading -> onLoading()
            is Resource.Success -> onSuccess(state.value)
            is Resource.Failed -> onFailed(state.error)
        }
    }
}


@Composable
fun CircularLoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}