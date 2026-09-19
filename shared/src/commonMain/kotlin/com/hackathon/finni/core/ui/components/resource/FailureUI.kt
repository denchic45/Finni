package com.hackathon.finni.core.ui.components.resource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hackathon.finni.api.error.UnauthorizedError
import com.hackathon.finni.core.presentation.error.ApiFailure
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.theme.spacing
import com.hackathon.finni.core.ui.components.EmptyContent
import com.hackathon.finni.core.ui.extension.getString
import com.hackathon.finni.core.ui.extension.getStringResource
import com.hackathon.finni.core.ui.extension.rememberPainter
import com.hackathon.finni.core.ui.navigation.Auth
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.push
import org.koin.compose.koinInject


@Composable
fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularLoadingBox() }
}

@Composable
fun BoxScope.DefaultCachedErrorSnackbar(
    error: UiError?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: (() -> Unit)?,
    retrying: Boolean,
) {
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(
                message = error.message.getString(),
                duration = SnackbarDuration.Indefinite
            )
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .imePadding()
    ) { snackbarData ->
        SwipeToDismissBox(
            state = rememberSwipeToDismissBoxState(),
            backgroundContent = {}
        ) {
            Snackbar(
                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                action = onRetry?.let {
                    {
                        TextButton(onClick = onRetry) {
                            if (!retrying)
                                Text("Повторить")
                            else
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                        }
                    }
                }
            ) {
                Text(snackbarData.visuals.message)
            }
        }
    }
}

@Composable
fun DefaultFailedContent(
    error: UiError,
    isRetrying: Boolean? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyContent(
        modifier = modifier,
        iconPainter = error.icon.rememberPainter(),
        title = { Text(text = error.title.getStringResource(), textAlign = TextAlign.Center) },
        description = if (error.origin is ApiFailure && error.origin.error is UnauthorizedError) {
            {
                Text(
                    text = "Откройте доступ к функциям приложения",
                    textAlign = TextAlign.Center
                )
            }
        } else null,
        action = if (error.origin is ApiFailure && error.origin.error is UnauthorizedError) {
            {
                val router = koinInject<Router>()
                Button(onClick = { router.push(Auth) }) {
                    Text("Войти")
                }
            }
        } else onRetry?.let {
            {
                Column(Modifier.height(48.dp).padding(top = MaterialTheme.spacing.small)) {
                    if (isRetrying == false)
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                        ) { Text("Повторить") }
                    else {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    )
}