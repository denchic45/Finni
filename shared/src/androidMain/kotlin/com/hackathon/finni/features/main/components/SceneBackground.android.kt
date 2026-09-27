package com.hackathon.finni.features.main.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
actual fun SceneBackground(
    modifier: Modifier,
    modelPath: String,
    hdrPath: String
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    // Фокус камеры — центр питомца Финни из Scene.glb
    val targetPosition = remember { Float3(x = 0.4f, y = 0.65f, z = 0.0f) }
    // Фиксированная дистанция орбиты (масштабирование/зум запрещено)
    val cameraDistance = 3.2f

    // Изометрический ракурс: поворот против часовой стрелки на ~22° и наклон сверху на ~14°
    val baseIsometricYaw = 22.0f
    val baseIsometricPitch = 14.0f

    // Текущие углы поворота камеры (в градусах):
    // yaw: поворот в бок, ограничен ±45° от базового изометрического ракурса
    // pitch: наклон по вертикали, ограничен [-2°, +24°] (камера гарантированно не касается пола Y=0.1 и потолка Y=3.0)
    var yaw by remember { mutableFloatStateOf(baseIsometricYaw) }
    var pitch by remember { mutableFloatStateOf(baseIsometricPitch) }

    // Начальная позиция камеры с изометрическим ракурсом
    val initialCameraPosition = remember {
        val radYaw = (baseIsometricYaw * (PI / 180.0)).toFloat()
        val radPitch = (baseIsometricPitch * (PI / 180.0)).toFloat()
        Float3(
            x = targetPosition.x + cameraDistance * cos(radPitch) * sin(radYaw),
            y = targetPosition.y + cameraDistance * sin(radPitch),
            z = targetPosition.z + cameraDistance * cos(radPitch) * cos(radYaw)
        )
    }

    val cameraNode = rememberCameraNode(engine) {
        position = initialCameraPosition
        lookAt(
            eye = initialCameraPosition,
            center = targetPosition,
            up = Float3(x = 0.0f, y = 1.0f, z = 0.0f)
        )
    }

    // Реактивное обновление положения камеры при свайпах с сохранением фокуса на питомце
    LaunchedEffect(yaw, pitch, cameraNode) {
        val radYaw = (yaw * (PI / 180.0)).toFloat()
        val radPitch = (pitch * (PI / 180.0)).toFloat()

        val camX = targetPosition.x + cameraDistance * cos(radPitch) * sin(radYaw)
        val camY = targetPosition.y + cameraDistance * sin(radPitch)
        val camZ = targetPosition.z + cameraDistance * cos(radPitch) * cos(radYaw)

        val newCameraPos = Float3(camX, camY, camZ)
        cameraNode.position = newCameraPos
        cameraNode.lookAt(
            eye = newCameraPos,
            center = targetPosition,
            up = Float3(x = 0.0f, y = 1.0f, z = 0.0f)
        )
    }

    val defaultEnv = rememberEnvironment(environmentLoader)
    val environment = remember(environmentLoader, hdrPath) {
        try {
            environmentLoader.createHDREnvironment(assetFileLocation = hdrPath) ?: defaultEnv
        } catch (e: Exception) {
            defaultEnv
        }
    }

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var isModelAddedToScene by remember { mutableStateOf(false) }
    var framesRenderedAfterAttach by remember { mutableIntStateOf(0) }
    var isSceneReady by remember { mutableStateOf(false) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isSceneReady) 0.0f else 1.0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        ),
        label = "scene_fade_in"
    )

    LaunchedEffect(modelLoader, modelPath) {
        try {
            val modelInstance = modelLoader.loadModelInstance(fileLocation = modelPath)
                ?: modelLoader.createModelInstance(assetFileLocation = modelPath)
            val node = ModelNode(
                modelInstance = modelInstance,
                autoAnimate = true
            ).apply {
                onAddedToScene = {
                    isModelAddedToScene = true
                }
            }
            modelNode = node
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Прогрев шейдеров и текстур в Filament перед запуском плавного появления
    LaunchedEffect(isModelAddedToScene) {
        if (isModelAddedToScene) {
            // Даем время на компиляцию шейдеров материалов и GPU буферизацию
            delay(400)
            try {
                engine.flushAndWait()
            } catch (e: Exception) {
                // ignore
            }
            isSceneReady = true
        }
    }

    // Защитный таймаут на случай непредвиденных сбоев
    LaunchedEffect(modelNode) {
        if (modelNode != null && !isSceneReady) {
            delay(1500)
            isSceneReady = true
        }
    }

    val childNodes = remember(modelNode) {
        listOfNotNull(modelNode)
    }

    Box(
        modifier = modifier
            .background(Color(0xFFD9D9D9))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val sensitivity = 0.18f
                    // Поворот в бок: ровно по ±45 градусов в обе стороны от изометрического ракурса
                    yaw = (yaw - dragAmount.x * sensitivity).coerceIn(
                        baseIsometricYaw - 45.0f,
                        baseIsometricYaw + 45.0f
                    )
                    // Наклон вверх/вниз: безопасно над полом и под потолком
                    pitch = (pitch + dragAmount.y * sensitivity).coerceIn(2f, 20.0f)
                }
            }
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            cameraNode = cameraNode,
            cameraManipulator = null,
            childNodes = childNodes,
            onFrame = {
                if (isModelAddedToScene && !isSceneReady) {
                    framesRenderedAfterAttach++
                    if (framesRenderedAfterAttach >= 25) {
                        isSceneReady = true
                    }
                }
            }
        )

        // Плавное растворение заглушки только когда 3D-сцена полностью скомпилирована и отрисована
        if (overlayAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = overlayAlpha }
                    .background(Color(0xFFD9D9D9))
            )
        }
    }
}
