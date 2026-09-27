package com.hackathon.finni.features.main.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 3D сцена фона для главного экрана Финни.
 * На Android рендерится через SceneView (Filament), загружая GLB-модель и HDR-карту окружения.
 * На Desktop (JVM) и iOS предоставляется заглушка.
 *
 * @param modifier Модификатор размера и расположения.
 * @param modelPath Путь к 3D-модели (в assets).
 * @param hdrPath Путь к карте освещения HDR (в assets).
 */
@Composable
expect fun SceneBackground(
    modifier: Modifier = Modifier,
    modelPath: String = "Scene.glb",
    hdrPath: String = "studio_small_09_1k.hdr"
)
