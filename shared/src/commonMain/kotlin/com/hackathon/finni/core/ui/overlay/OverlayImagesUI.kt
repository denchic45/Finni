package com.hackathon.finni.core.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.compose.rememberAsyncImagePainter
import com.hackathon.finni.core.ui.dialog.FullScreenDialog
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_arrow_back
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayImagesScreen(
    imageUrls: List<String>,
    initialIndex: Int,
    viewModel: OverlayImagesViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imageUrls.size },
    )
    val coroutineScope = rememberCoroutineScope()

    var controlsVisible by remember { mutableStateOf(true) }

    val offsetY = remember { Animatable(0f) }
    val backgroundAlpha = (1f - (abs(offsetY.value) / 800f)).coerceIn(0f, 1f)



    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = { viewModel.onDismiss(pagerState.currentPage) }
    )

    FullScreenDialog(onDismissRequest = { viewModel.onDismiss(pagerState.currentPage) }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backgroundAlpha))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp
            ) { page ->
                val painter = rememberAsyncImagePainter(model = imageUrls[page])
                val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)

                val viewConfiguration = LocalViewConfiguration.current
                val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
                var tapJob by remember { mutableStateOf<Job?>(null) }

// Защита: если при двойном тапе сработал зум и изменился масштаб,
// мы гарантированно отменяем скрытие/показ панелей
                LaunchedEffect(zoomState.scale) {
                    tapJob?.cancel()
                }

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(zoomState)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .pointerInput(zoomState.scale) {
                            if (zoomState.scale <= 1f) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val pointerId = awaitFirstDown().id
                                        var totalPanY = 0f
                                        var totalPanX = 0f
                                        var isDraggingVertical = false
                                        var isDraggingHorizontal = false

                                        do {
                                            val event = awaitPointerEvent()
                                            val change =
                                                event.changes.firstOrNull { it.id == pointerId }

                                            if (change != null && change.pressed) {
                                                val dragAmount = change.positionChange()
                                                totalPanY += dragAmount.y
                                                totalPanX += dragAmount.x

                                                if (!isDraggingVertical && !isDraggingHorizontal) {
                                                    if (abs(totalPanY).dp > 100.dp && abs(totalPanY) > abs(
                                                            totalPanX
                                                        )
                                                    ) {
                                                        isDraggingVertical = true
                                                    } else if (abs(totalPanX) > 10f) {
                                                        isDraggingHorizontal = true
                                                    }
                                                }

                                                if (isDraggingVertical) {
                                                    change.consume()
                                                    coroutineScope.launch {
                                                        offsetY.snapTo(offsetY.value + dragAmount.y)
                                                    }
                                                }
                                            }
                                        } while (change != null && change.pressed)

                                        if (isDraggingVertical) {
                                            if (abs(offsetY.value) > 400f) {
                                                viewModel.onDismiss(pagerState.currentPage)
                                            } else {
                                                coroutineScope.launch {
                                                    offsetY.animateTo(
                                                        0f,
                                                        spring(Spring.DampingRatioLowBouncy)
                                                    )
                                                }
                                            }
                                        } else if (!isDraggingHorizontal) {
                                            // --- МОДИФИЦИРОВАННЫЙ БЛОК ДЛЯ СИНГЛ-ТАПА ---
                                            if (tapJob?.isActive == true) {
                                                // Если это второй тап в рамках тайм-аута — отменяем переключение
                                                tapJob?.cancel()
                                            } else {
                                                // Если это первый тап — ждем, не будет ли второго
                                                tapJob = coroutineScope.launch {
                                                    delay(doubleTapTimeout.milliseconds)
                                                    controlsVisible = !controlsVisible
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitFirstDown()
                                        val up = waitForUpOrCancellation()
                                        if (up != null) {
                                            // --- МОДИФИЦИРОВАННЫЙ БЛОК ДЛЯ ЗУМ-СОСТОЯНИЯ ---
                                            if (tapJob?.isActive == true) {
                                                tapJob?.cancel()
                                            } else {
                                                tapJob = coroutineScope.launch {
                                                    delay(doubleTapTimeout.milliseconds)
                                                    controlsVisible = !controlsVisible
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }


            AnimatedVisibility(
                visible = controlsVisible,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> -fullHeight }, // Входит сверху (от -height до 0)
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> -fullHeight }, // Уходит вверх (от 0 до -height)
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(0.5f)
                    ),
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.onDismiss(pagerState.currentPage) },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                )
            }

            if (imageUrls.size > 1) {
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight }, // Входит снизу (от height до 0)
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight }, // Уходит вниз (от 0 до height)
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    CurrentPhotoIndicator(pagerState, imageUrls)
                }
            }
        }
    }
}

@Composable
private fun CurrentPhotoIndicator(
    pagerState: PagerState,
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(bottom = 32.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${pagerState.currentPage + 1} из ${imageUrls.size}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}